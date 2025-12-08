package site.yuanshen.data.helper.transformer.base.config;

import cn.hutool.core.util.StrUtil;
import org.jsoup.nodes.Document;
import site.yuanshen.data.helper.transformer.base.extensions.ExtensionInterface;

import java.util.*;

public final class TransformConfig {
    public final Map<String, ExtensionInterface> extensions = new HashMap<>();

    public final List<String> extensionOrders = new ArrayList();

    public TransformConfig registerExtension(String name, ExtensionInterface extension) {
        if (StrUtil.isBlank(name)) {
            return this;
        }

        this.extensions.put(name, extension);
        this.extensionOrders.add(name);

        return this;
    }

    public TransformConfig unregisterExtension(String name) {
        if (StrUtil.isBlank(name)) {
            return this;
        }

        this.extensions.remove(name);
        this.extensionOrders.remove(name);

        return this;
    }

    public TransformConfig setOrders(String... orders) {
        final List<String> orderList = new ArrayList<>(Arrays.asList(orders));
        this.extensionOrders.clear();
        this.extensionOrders.addAll(orderList);
        return this;
    }

    public TransformConfig moveExtensionBefore(String findName, String beforeName) {
        if (StrUtil.isBlank(findName) || StrUtil.isBlank(beforeName)) {
            return this;
        }

        // Test whether source and target are present
        int findIndex = this.extensionOrders.indexOf(findName);
        int beforeIndex = this.extensionOrders.indexOf(beforeName);
        if (findIndex == -1 || beforeIndex == -1) {
            // Not found
            return this;
        }

        this.extensionOrders.remove(findIndex);
        // Redo find to avoid array shift effect
        // No need to test absence because of previous test
        beforeIndex = this.extensionOrders.indexOf(beforeName);
        this.extensionOrders.add(beforeIndex, findName);

        return this;
    }

    public TransformConfig moveExtensionAfter(String findName, String afterName) {
        if (StrUtil.isBlank(findName) || StrUtil.isBlank(afterName)) {
            return this;
        }

        // Test whether source and target are present
        int findIndex = this.extensionOrders.indexOf(findName);
        int afterIndex = this.extensionOrders.indexOf(afterName);
        if (findIndex == -1 || afterIndex == -1) {
            // Not found
            return this;
        }

        this.extensionOrders.remove(findIndex);
        // Redo find to avoid array shift effect
        // No need to test absence because of previous test
        afterIndex = this.extensionOrders.indexOf(afterName);
        this.extensionOrders.add(afterIndex + 1, findName);

        return this;
    }

    public void applyTransformers(Document doc) {
        if (doc == null) {
            return;
        }

        this.extensionOrders.forEach(name -> {
            if (StrUtil.isBlank(name)) {
                return;
            }

            ExtensionInterface extension = this.extensions.get(name);
            if (extension == null) {
                return;
            }

            extension.transform(doc);
        });
    }
}
