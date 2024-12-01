package dms.project.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.databasemigration.DatabaseMigrationClient;
import software.amazon.awssdk.services.ec2.Ec2Client;

@Configuration
public class AwsConfig {

    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;

    @Bean
    public DatabaseMigrationClient databaseMigrationClient() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
                accessKey, secretKey);

        return DatabaseMigrationClient.builder()
                .region(Region.of("ap-northeast-2"))  // 사용할 AWS 리전으로 변경
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))  // 기본 자격 증명 제공자 사용
                .build();
    }

    @Bean
    public Ec2Client ec2Client() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
                accessKey, secretKey);

        return Ec2Client.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }

}
