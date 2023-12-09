<?php
require_once __DIR__ . DIRECTORY_SEPARATOR . './uploader/img_compressor.php';

header('Access-Control-Allow-Origin: *');
if($_SERVER['REQUEST_METHOD'] == 'OPTIONS') {
    exit;
}

function upload_image($file_data = '', $file_name = '') {
    $res = (object)[
        'status' => TRUE,
        'msg' => '',
        'err_info' => '',
        'path' => ''
    ];

    $target_path = "./saved_img/${file_name}.jpg";

    try {
        $compressor = (new \ImageCompressor\Compressor())
            ->set_cache('./cache')
            ->load_image($file_data)
            ->create_thumb(256, 256);
        $image = $compressor
            ->compress_by_filesize(30 * 1024)
            ->save_image(
                \ImageCompressor\Constants::IMAGE_COMPRESSED,
                $target_path
            );

        $res->status = TRUE;
        $res->msg = '上传压缩成功';
        $res->path = substr($target_path, 1);
    } catch (Exception $e) {
        $res->status = FALSE;
        $res->msg = '上传失败';
        $res->err_info = $e->getMessage();
    } finally {
        // 手动触发文件删除
        $image = NULL;
        $compressor = NULL;
    }

    return $res;
}

header('Content-Type: application/json');
$file_data = $_POST['file_data'];
$file_name = $_POST['file_name'];
$res = upload_image($file_data, $file_name);
echo json_encode($res, JSON_UNESCAPED_UNICODE);
