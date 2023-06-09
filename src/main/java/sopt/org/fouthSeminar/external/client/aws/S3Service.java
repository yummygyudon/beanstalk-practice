package sopt.org.fouthSeminar.external.client.aws;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sopt.org.fouthSeminar.exception.Error;
import sopt.org.fouthSeminar.exception.model.BadRequestException;
import sopt.org.fouthSeminar.exception.model.NotFoundException;

import javax.annotation.PostConstruct;
import javax.lang.model.type.ArrayType;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;
    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${cloud.aws.region.static}")
    private String region;

    @PostConstruct
    public AmazonS3Client amazonS3Client(){
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        return (AmazonS3Client) AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }

    public String uploadImage(MultipartFile multipartFile, String folder) {
        String fileName = createFileName(multipartFile.getOriginalFilename());
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(multipartFile.getSize());
        objectMetadata.setContentType(multipartFile.getContentType());

        try (InputStream inputStream = multipartFile.getInputStream()) {
            amazonS3.putObject(new PutObjectRequest(bucket + "/" + folder + "/image", fileName, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
            return amazonS3.getUrl(bucket + "/" + folder + "/image", fileName).toString();
        } catch (IOException e) {
            throw new NotFoundException(Error.NOT_FOUND_SAVE_IMAGE_EXCEPTION, Error.NOT_FOUND_SAVE_IMAGE_EXCEPTION.getMessage());
        }
    }

    public List<String> uploadImages(List<MultipartFile> multipartFiles, String folder) {
        List<String> imagePathList = new ArrayList<>();
        for (MultipartFile file : multipartFiles) {
            String imagePath = uploadImage(file, folder);
            imagePathList.add(imagePath);
        }
        return imagePathList;
    }

    private String createFileName(String fileName) {
        return UUID.randomUUID().toString().concat(getFileExtension(fileName));
    }


    private String getFileExtension(String fileName) {
        if (fileName.length() == 0) {
            throw new NotFoundException(Error.NOT_FOUND_IMAGE_EXCEPTION, Error.NOT_FOUND_IMAGE_EXCEPTION.getMessage());
        }
        ArrayList<String> fileValidate = new ArrayList<>();
        fileValidate.add(".jpeg");
        fileValidate.add(".png");
        fileValidate.add(".JPG");
        fileValidate.add(".JPEG");
        fileValidate.add(".PNG");
        String idxFileName = fileName.substring(fileName.lastIndexOf("."));
        if (!fileValidate.contains(idxFileName)) {
            throw new BadRequestException(Error.INVALID_MULTIPART_EXTENSION_EXCEPTION, Error.INVALID_MULTIPART_EXTENSION_EXCEPTION.getMessage());
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
