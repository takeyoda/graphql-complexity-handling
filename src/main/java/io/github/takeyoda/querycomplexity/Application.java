package io.github.takeyoda.querycomplexity;

// import org.graalvm.nativeimage.ImageInfo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder.SpringWebBlockHoundIntegration;
import reactor.blockhound.BlockHound;

@SpringBootApplication
public class Application {

  private static final boolean IN_NATIVE_IMAGE;
  static {
    // https://github.com/oracle/graal/blob/master/sdk/src/org.graalvm.nativeimage/src/org/graalvm/nativeimage/ImageInfo.java
    final String imagecode = System.getProperty("org.graalvm.nativeimage.imagecode");
    IN_NATIVE_IMAGE = "buildtime".equals(imagecode) || "runtime".equals(imagecode);
  }
  public static void main(String[] args) {
    if (!IN_NATIVE_IMAGE) {
      BlockHound.builder().with(new SpringWebBlockHoundIntegration()).install();
    }
    SpringApplication.run(Application.class, args);
  }
}
