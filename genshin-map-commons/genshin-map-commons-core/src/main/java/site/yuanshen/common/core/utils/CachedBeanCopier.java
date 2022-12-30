package site.yuanshen.common.core.utils;
/**
 * @author : seinonana
 * @className : MyBeanCopier
 * @description :
 * @date: 2021-01-12 16:53
 */
/*
 * Copyright 2003,2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.Type;
import org.springframework.cglib.core.*;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

abstract public class CachedBeanCopier {
    private static final String SET_PREFIX = "set";

    private static final Logger logger = LoggerFactory.getLogger(CachedBeanCopier.class);
    private static final Map<String, CachedBeanCopier> beanCopierCacheMap = new ConcurrentHashMap<>();

    private static final BeanCopierKey KEY_FACTORY = (BeanCopierKey) KeyFactory.create(BeanCopierKey.class);
    private static final Type BEAN_COPIER = TypeUtils.parseType(CachedBeanCopier.class.getName());
    private static final Signature COPY = new Signature("copy", Type.VOID_TYPE, new Type[]{Constants.TYPE_OBJECT, Constants.TYPE_OBJECT});

    interface BeanCopierKey {
        Object newInstance(String source, String target, boolean useConverter);
    }

    public static CachedBeanCopier create(Class source, Class target, boolean useConverter) {
        Generator gen = new Generator();
        gen.setSource(source);
        gen.setTarget(target);
        gen.setUseConverter(useConverter);
        return gen.create();
    }

    public static <T, R> T copyProperties(R r, T t) {
        String cacheKey = r.getClass().toString() + t.getClass().toString();
        CachedBeanCopier beanCopier;
        // 线程1和线程2，同时过来了
        if (!beanCopierCacheMap.containsKey(cacheKey)) {
            // 两个线程都卡这儿了
            // 但是此时线程1先获取到了锁，线程2就等着
            synchronized (CachedBeanCopier.class) {
                // 线程1进来之后，发现这里还是没有那个BeanCopier实例
                // 此时线程2，会发现缓存map中已经有了那个BeanCopier实例了，此时就不会进入if判断内的代码
                if (!beanCopierCacheMap.containsKey(cacheKey)) {
                    // 进入到这里会创建一个BeanCopier实例并且放在缓存map中
                    beanCopier = CachedBeanCopier.create(r.getClass(), t.getClass(), false);
                    beanCopierCacheMap.put(cacheKey, beanCopier);
                } else {
                    beanCopier = beanCopierCacheMap.get(cacheKey);
                }
            }
        } else {
            beanCopier = beanCopierCacheMap.get(cacheKey);
        }
        beanCopier.copy(r, t);
        return t;
    }

    public static <T, R> T copyProperties(R r, Class<T> clazz) {
        T target = null;
        try {
            target = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            logger.error("克隆异常", e);
        }
        return copyProperties(r, target);
    }

    abstract public void copy(Object from, Object to);

    public static class Generator extends AbstractClassGenerator {
        private static final Source SOURCE = new Source(CachedBeanCopier.class.getName());
        private Class source;
        private Class<?> target;
        private boolean useConverter;

        public Generator() {
            super(SOURCE);
        }

        public void setSource(Class source) {
            if (!Modifier.isPublic(source.getModifiers())) {
                setNamePrefix(source.getName());
            }
            this.source = source;
        }

        public void setTarget(Class target) {
            if (!Modifier.isPublic(target.getModifiers())) {
                setNamePrefix(target.getName());
            }
            this.target = target;
        }

        public void setUseConverter(boolean useConverter) {
            this.useConverter = useConverter;
        }

        @Override
        protected ClassLoader getDefaultClassLoader() {
            return source.getClassLoader();
        }

        public CachedBeanCopier create() {
            Object key = KEY_FACTORY.newInstance(source.getName(), target.getName(), useConverter);
            return (CachedBeanCopier) super.create(key);
        }

        @Override
        public void generateClass(ClassVisitor v) throws IntrospectionException, ClassNotFoundException {
            Type sourceType = Type.getType(source);
            Type targetType = Type.getType(target);
            ClassEmitter ce = new ClassEmitter(v);
            ce.begin_class(Constants.V1_2,
                    Constants.ACC_PUBLIC,
                    getClassName(),
                    BEAN_COPIER,
                    null,
                    Constants.SOURCE_FILE);
            EmitUtils.null_constructor(ce);
            CodeEmitter e = ce.begin_method(Constants.ACC_PUBLIC, COPY, null);
            PropertyDescriptor[] getters = ReflectUtils.getBeanGetters(source);

            List<PropertyDescriptor> setterList = new ArrayList<>();
            Method[] result = target.getMethods();
            //查找set方法
            for (int i = 0; i < result.length; i++) {
                Method method = result[i];
                if (!method.getDeclaringClass().equals(target) || Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
                String name = method.getName();
                Class<?>[] argTypes = method.getParameterTypes();
                int argCount = argTypes.length;
                if (argCount == 1 && name.startsWith(SET_PREFIX)) {
                    PropertyDescriptor propertyDescriptor = new PropertyDescriptor(Introspector.decapitalize(name.substring(3)), null, method);
                    setterList.add(propertyDescriptor);
                }
            }
            PropertyDescriptor[] setters = setterList.toArray(new PropertyDescriptor[setterList.size()]);
            Map<String, PropertyDescriptor> names = new HashMap(16);
            for (int i = 0; i < getters.length; i++) {
                names.put(getters[i].getName(), getters[i]);
            }
            //将Object类型强转为要转换的类型
            Local targetLocal = e.make_local();
            Local sourceLocal = e.make_local();
            e.load_arg(1);
            e.checkcast(targetType);
            e.store_local(targetLocal);
            e.load_arg(0);
            e.checkcast(sourceType);
            e.store_local(sourceLocal);
            //生成每个setter和getter方法
            for (int i = 0; i < setters.length; i++) {
                PropertyDescriptor setter = setters[i];
                PropertyDescriptor getter = names.get(setter.getName());
                if (getter != null && setter != null) {
                    Method readMethod = getter.getReadMethod();
                    Method writeMethod = setter.getWriteMethod();
                    //set的写方法没找到,或者set的写方法没有参数
                    if (writeMethod == null || readMethod == null) {
                        continue;
                    }
                    MethodInfo read = ReflectUtils.getMethodInfo(readMethod);
                    MethodInfo write = ReflectUtils.getMethodInfo(writeMethod);
                    if (compatible(getter, setter)) {
                        e.load_local(targetLocal);
                        e.load_local(sourceLocal);
                        e.invoke(read);
                        e.invoke(write);
                        if (!writeMethod.getReturnType().equals(void.class)) {
                            e.pop();
                        }
                    }
                }
            }
            e.return_value();
            e.end_method();
            ce.end_class();
        }

        private static boolean compatible(PropertyDescriptor getter, PropertyDescriptor setter) {
            return setter.getPropertyType().isAssignableFrom(getter.getPropertyType());
        }

        @Override
        protected Object firstInstance(Class type) {
            return ReflectUtils.newInstance(type);
        }

        @Override
        protected Object nextInstance(Object instance) {
            return instance;
        }
    }
}



