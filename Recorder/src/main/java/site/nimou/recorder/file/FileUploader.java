package site.nimou.recorder.file;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.MinioException;

/**
 * @author Ni Jiajun njj1108@outlook.com
 * @since 1.0.0 2024-05-06
 **/
public class FileUploader {

    public static final String BUCKETNAME = "myr";

    public static void main(String[] args) throws Exception {
        try {
            MinioClient minioClient =
                    MinioClient.builder()
                            .endpoint("http://172.18.10.150:9000")
                            .credentials("5LgBpCWx4NutbR1MJDCa", "rmpgQx3oabcVUgvcG61DUXNxx92XKFgauFTodtid")
                            .build();
            boolean found =
                    minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKETNAME).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKETNAME).build());
            } else {
                System.out.println("Bucket " + BUCKETNAME + " already exists.");
            }
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(BUCKETNAME)
                            .object("20240430174447.wav")
                            .filename("D:\\MYR\\20240430174447.wav")
                            .build());
        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
            System.out.println("HTTP trace: " + e.httpTrace());
        }
    }
}