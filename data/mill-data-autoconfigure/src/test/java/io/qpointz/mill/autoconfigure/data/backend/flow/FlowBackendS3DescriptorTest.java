package io.qpointz.mill.autoconfigure.data.backend.flow;

import io.qpointz.mill.autoconfigure.data.SqlAutoConfiguration;
import io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration;
import io.qpointz.mill.autoconfigure.data.resource.BackendResourceLoaderAutoConfiguration;
import io.qpointz.mill.cloud.aws.autoconfigure.S3AutoConfiguration;
import io.qpointz.mill.data.backend.dispatchers.SubstraitDispatcher;
import io.qpointz.mill.data.backend.flow.SourceDefinitionRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Flow backend loads a descriptor YAML from {@code s3://} when the AWS autoconfigure protocol resolver is present.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FlowBackendS3DescriptorTest {

    private static final String BUCKET = "mill-flow-s3-desc";

    private static final GenericContainer<?> MINIO = new GenericContainer<>(DockerImageName.parse("minio/minio:latest"))
            .withExposedPorts(9000)
            .withEnv("MINIO_ROOT_USER", "minioadmin")
            .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
            .withCommand("server", "/data");

    static {
        MINIO.start();
        seedMinio();
    }

    private static void seedMinio() {
        String endpoint = "http://" + MINIO.getHost() + ":" + MINIO.getMappedPort(9000);
        try (S3Client s3 = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.US_EAST_1)
                .credentialsProvider(
                        StaticCredentialsProvider.create(AwsBasicCredentials.create("minioadmin", "minioadmin")))
                .forcePathStyle(true)
                .build()) {
            s3.createBucket(CreateBucketRequest.builder().bucket(BUCKET).build());
            String yaml;
            try (InputStream in = FlowBackendS3DescriptorTest.class.getResourceAsStream("/flow/classpath-flow-test.yaml")) {
                assertThat(in).isNotNull();
                yaml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
            s3.putObject(
                    PutObjectRequest.builder().bucket(BUCKET).key("flow/descriptor.yaml").build(),
                    RequestBody.fromString(yaml));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @AfterAll
    static void stopMinio() {
        MINIO.stop();
    }

    @Test
    void shouldLoadDescriptorYamlFromS3Location() {
        String endpoint = "http://" + MINIO.getHost() + ":" + MINIO.getMappedPort(9000);
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        S3AutoConfiguration.class,
                        SqlAutoConfiguration.class,
                        BackendAutoConfiguration.class,
                        BackendResourceLoaderAutoConfiguration.class,
                        FlowBackendAutoConfiguration.class,
                        FlowDescriptorMetadataSourceAutoConfiguration.class))
                .withBean(SubstraitDispatcher.class, () -> mock(SubstraitDispatcher.class))
                .withPropertyValues(
                        "mill.data.backend.type=flow",
                        "mill.cloud.aws.s3.endpoint=" + endpoint,
                        "mill.cloud.aws.s3.region=us-east-1",
                        "mill.cloud.aws.s3.access-key=minioadmin",
                        "mill.cloud.aws.s3.secret-key=minioadmin",
                        "mill.data.backend.flow.sources[0]=s3://" + BUCKET + "/flow/descriptor.yaml")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    SourceDefinitionRepository repo = context.getBean(SourceDefinitionRepository.class);
                    var names = StreamSupport.stream(repo.getSourceDefinitions().spliterator(), false)
                            .map(d -> d.getName())
                            .toList();
                    assertThat(names).containsExactly("classpathflowtest");
                });
    }
}
