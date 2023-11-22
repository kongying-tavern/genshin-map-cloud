<?php
namespace ImageCompressor;

/**
 * -----------------------------
 * 错误类封装
 * -----------------------------
 */
/**
 * 参数错误类
 */
class ParameterException extends \Exception {}

/**
 * 缓存错误错误类
 */
class CacheException extends \Exception {}

/**
 * 图像错误类
 */
class ImageException extends \Exception {}

/**
 * -----------------------------
 * 常量类封装
 * -----------------------------
 */
final class Constants {
    public const IMAGE_ORIGINAL = 1;
    public const IMAGE_COMPRESSED = 2;
}

/**
 * -----------------------------
 * 辅助类封装
 * -----------------------------
 */
final class Utils {
    public static function uuid() {
        return md5(time() . mt_rand(1, 10000));
    }

    public static function mkdir($path = '', $message = '') {
        if(!is_dir($path)) {
            $mkdir_success = mkdir($path, 0777, TRUE);
            if(!$mkdir_success) {
                throw new CacheException($message ?: '无法创建目录');
            }
        }
    }

    public static function filename($path = '') {
        $filename = pathinfo($path, PATHINFO_FILENAME) ?: '';
        return $filename;
    }

    public static function extname($path = '') {
        $ext = pathinfo($path, PATHINFO_EXTENSION) ?: '';
        return $ext;
    }
}

/**
 * -----------------------------
 * 压缩相关类封装
 * -----------------------------
 */
/**
 * 缓存类
 */
class Cache {
    private $cache_dir = '';

    private function check_cache() {
        if(!is_dir($this->cache_dir)) {
            throw new CacheException('图片缓存目录不是一个有效的文件夹');
        }
    }

    public function set_cache($cache_dir = '') {
        if(empty($cache_dir)) {
            throw new CacheException('缓存目录不能为空');
        }
        $cache_dir = realpath($cache_dir);
        Utils::mkdir($cache_dir, '无法创建缓存目录');
        $this->cache_dir = $cache_dir;
        return $this;
    }

    public function get_cache_path($path = '') {
        $this->check_cache();
        return $this->cache_dir . DIRECTORY_SEPARATOR . $path;
    }
}

/**
 * 图像类
 */
class Image {
    private $cache = NULL;
    private $path = '';
    private $image = NULL;
    private $compress_path = NULL;
    private $compress_image = NULL;
    private $info = NULL;

    private const VALID_EXT = ['jpg', 'jpeg', 'png', 'bmp', 'wbmp', 'gif'];

    public function __construct() {
        $this->info = new ImageInfo();
    }

    public function __destruct() {
        $this->info = NULL;
        $this->clean_image();
        $this->clean_compress();
    }

    public function __set($name, $value) {
        if(in_array($name, ['info'], TRUE)) {
            $this->$name = $value;
        }
    }

    public function __get($name) {
        return $this->$name;
    }

    /**
     * -------------------------
     * 辅助方法
     * -------------------------
     */
    private function clean_image() {
        if(is_resource($this->image))
            \imagedestroy($this->image);
        if(is_file($this->path))
            @unlink($this->path);
    }

    private function clean_compress() {
        if(is_resource($this->compress_image))
            \imagedestroy($this->compress_image);
        if(is_file($this->compress_path))
            @unlink($this->compress_path);
    }

    /**
     * -------------------------
     * 缓存相关
     * -------------------------
     */
    private function check_cache(){
        if(empty($this->cache)) {
            throw new CacheException('图片缓存未设置，请先设置缓存');
        }
    }

    public function update_cache(&$cache) {
        $this->cache = $cache;
        return $this;
    }

    /**
     * -------------------------
     * 图片相关
     * -------------------------
     */
    private function save_resource_file($src) {
        $this->check_cache();
        $this->clean_image();
        $img_path = $this->cache->get_cache_path(Utils::uuid() . '.bin.jpg');
        \imagejpeg($src, $img_path, 100);
        return $img_path;
    }

    private function save_uri_file($src = '') {
        $this->check_cache();
        $this->clean_image();
        $img_content = file_get_contents($src);
        $img_path = $this->cache->get_cache_path(Utils::uuid() . '.bin');
        file_put_contents($img_path, $img_content);
        return $img_path;
    }

    private function save_local_file($src = '') {
        $this->check_cache();
        $this->clean_image();
        $img_path = $this->cache->get_cache_path(Utils::uuid() . '.bin.cpf');
        copy($src, $img_path);
        return $img_path;
    }

    public function load_image($src) {
        if(is_resource($src)) {
            // 载入资源数据
            $src = $this->save_resource_file($src);
        } else if(
            is_string($src) && (
                strpos($src, 'data:') === 0 ||
                strpos($src, 'file://') === 0 ||
                strpos($src, 'http://') === 0 ||
                strpos($src, 'https://') === 0 ||
                strpos($src, 'ftp://') === 0 ||
                strpos($src, 'ftps://') === 0
            )
        ) {
            // 转存非本地文件
            $src = $this->save_uri_file($src);
        } else if(is_string($src) && is_file($src)) {
            // 转存本地文件
            $src = $this->save_local_file($src);
        } else {
            throw new ImageException('传入的图片源无效');
        }


        $img_info = $this->get_image_info($src);
        $img_creator = "\imagecreatefrom{$img_info->ext}";
        $this->path = $src;
        $this->image = $img_creator($src);
        $this->set_dimension($img_info->width, $img_info->height);
        $this->info->type = $img_info->type;
        $this->info->ext = $img_info->ext;
        $this->info->mime = $img_info->mime;
        $img_info = NULL;

        return $this;
    }

    /**
     * -------------------------
     * 压缩相关
     * -------------------------
     */
    private function check_compress_rate($rate = 100) {
        if(!is_numeric($rate)) {
            throw new ParameterException('压缩质量需为数字');
        } else if($rate < 0 || $rate > 100) {
            throw new ParameterException('压缩质量需为0-100的数字');
        }
    }

    public function compress($rate = 100) {
        $this->check_cache();
        $this->check_compress_rate($rate);

        if(!is_resource($this->image)) {
            throw new ImageException('源图片无效，无法进行压缩');
        }

        // 创建压缩图片
        $this->clean_compress();
        $img_path = $this->cache->get_cache_path(Utils::uuid() . '.bin.cpr');
        \imagejpeg($this->image, $img_path, $rate);

        // 加载压缩
        $img_info = $this->get_image_info($img_path);
        $img_creator = "\imagecreatefrom{$img_info->ext}";
        $this->compress_path = $img_path;
        $this->compress_image = $img_creator($img_path);
        $img_info = NULL;

        return $this;
    }

    /**
     * -------------------------
     * 图像信息相关
     * -------------------------
     */
    public function get_image_info($img_path) {
        $img_info = getimagesize($img_path);
        $width = $img_info[0] ?? 0;
        $height = $img_info[1] ?? 0;
        $type = $img_info[2] ?? \IMAGE_JPG;
        $ext = image_type_to_extension($type, FALSE);
        $mime = $img_info['mime'] ?? '';

        return (object) [
            'width' => $width,
            'height' => $height,
            'type' => $type,
            'ext' => $ext,
            'mime' => $mime
        ];
    }

    public function set_dimension($width = 0, $height = 0) {
        $this->info->width = $width;
        $this->info->height = $height;
    }

    /**
     * -------------------------
     * 图像存储相关
     * -------------------------
     */
    private function get_save_image($type = Constants::IMAGE_COMPRESSED) {
        $img = NULL;
        switch($type) {
            case Constants::IMAGE_COMPRESSED:
                $img = $this->image;
                break;
            case Constants::IMAGE_ORIGINAL:
                $img = $this->compress_image;
                break;
        }

        if(!is_resource($img)) {
            throw new ImageException('无法获取需要保存的图片，保存失败');
        }

        return $img;
    }

    public function save_image($type, $path = '') {
        if(empty($path)) {
            throw new ParameterException('图片保存路径不能为空');
        }

        $img = $this->get_save_image($type);
        $dirname = dirname($path);
        $filename = Utils::filename($path);
        $ext = strtolower(Utils::extname($path));
        if(!in_array($ext, self::VALID_EXT, TRUE)) {
            $ext = $this->info->ext;
        }
        if(empty($filename)) {
            throw new ParameterException('文件名不能为空');
        }

        Utils::mkdir($dirname, '无法创建存储目录');
        $save_path = $dirname . \DIRECTORY_SEPARATOR . $filename . '.' . $ext;
        $saver_map = ['jpg' => '\imagejpeg'];
        $saver = $saver_map[$ext] ?: "\image${ext}";
        $saver($img, $save_path);

        return $this;
    }
}

/**
 * 图像信息类
 */
class ImageInfo {
    public $width = 0;
    public $height = 0;
    public $type = NULL;
    public $ext = '';
    public $mime = '';
}

/**
 * 压缩器类
 */
class Compressor {
    private $cache;
    private $image;
    private $thumb;

    public function __construct() {
        $this->cache = new Cache();
        $this->image = new Image();
        $this->thumb = new Image();
    }

    public function __destruct() {
        $this->cache = NULL;
        $this->image = NULL;
        $this->thumb = NULL;
    }

    /**
     * -------------------------
     * 缓存相关
     * -------------------------
     */
    public function set_cache($cache_dir = '') {
        $this->cache->set_cache($cache_dir);
        $this->image->update_cache($this->cache);
        $this->thumb->update_cache($this->cache);
        return $this;
    }

    /**
     * -------------------------
     * 图像和缓存相关
     * -------------------------
     */
    public function load_image($src = '') {
        if(empty($src)) {
            throw new ImageException('图片路径不能为空');
        }
        $this->image->load_image($src);
        return $this;
    }

    public function create_thumb($width = 0, $height = 0) {
        if(!is_resource($this->image->image)) {
            throw new ImageException('原图未加载，无法生成缩略图');
        }
        $img_thumb = imagecreatetruecolor($width, $height);
        imagecopyresampled(
            $img_thumb,
            $this->image->image,
            0, 0, 0, 0,
            $width, $height,
            $this->image->info->width, $this->image->info->height
        );
        $this->thumb->load_image($img_thumb);
        $this->thumb->set_dimension($width, $height);

        return $this;
    }

    public function get_min_image() {
        $img = NULL;
        if(is_resource($this->thumb->image)) {
            $img = $this->thumb->image;
        } else if(is_resource($this->image->image)) {
            $img = $this->image->image;
        }

        if(!is_resource($img)) {
            throw new ImageException('无有效的图片或缩略图');
        }

        return $img;
    }

    /**
     * -------------------------
     * 压缩相关
     * -------------------------
     */
    public function compress_by_quality($compress_rate = 100) {
        $img_src = $this->get_min_image();
        $img = (new Image())
            ->update_cache($this->cache)
            ->load_image($img_src);
        $img->compress($compress_rate);
        return $img;
    }

    public function compress_by_filesize($limit_size = 0) {
        if(!is_numeric($limit_size)) {
            throw new ParameterException('文件大小需为数字');
        } else if($limit_size <= 0) {
            throw new ParameterException('文件大小需为正数');
        }

        $img_src = $this->get_min_image();
        $img = (new Image())
            ->update_cache($this->cache)
            ->load_image($img_src);

        // 降低压缩比率限制文件大小阈值范围
        $img_rate = 100;
        for(; $img_rate >= 0; $img_rate -= 5) {
            clearstatcache();
            $img->compress($img_rate);
            $img_size = filesize($img->compress_path);
            if($img_size <= $limit_size) {
                break;
            }
        }

        // 提高压缩比例逼近文件大小阈值
        $img_rate_offset = 1;
        if($img_rate >= 100) {
            $img->compress(100);
            return $img;
        } else if($img_rate <= 0) {
            $img->compress(0);
            return $img;
        } else {
            for(; $img_rate_offset <= 5; $img_rate_offset++) {
                clearstatcache();
                $img->compress($img_rate + $img_rate_offset);
                $img_size = filesize($img->compress_path);
                if($img_size > $limit_size) {
                    $img_rate_offset--;
                    break;
                }
            }

            // 重新构造压缩图片
            $img->compress($img_rate + $img_rate_offset);
            return $img;
        }
    }
}
