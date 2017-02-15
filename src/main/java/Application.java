import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;

@Controller
@EnableAutoConfiguration
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private final ExecutorService executor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
    );

    private final List<Pattern> patterns = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void init() throws IOException {
        log.info("Reading patterns...");
        Files.readAllLines(Paths.get(System.getProperty("user.dir"), "patterns.txt")
        ).stream().filter(s -> !s.isEmpty())
                .map(s -> Pattern.compile(s.replace(
                        "%w+",
                        "[\\wА-яЁё \\Q!№#%.,:;?\\/()+-“”―_'\"`&^?{}[]<>/\\|!@#$%^()+=~*\\E]*")))
                .forEach(patterns::add);
        log.info("Read patterns: {}", patterns.size());
    }

    @RequestMapping("/validate")
    @ResponseBody
    public DeferredResult<ResponseEntity> validate(@RequestParam String text) {
        DeferredResult<ResponseEntity> res = new DeferredResult<>();
        CompletableFuture.supplyAsync(
                () -> patterns.stream().anyMatch(p -> p.matcher(text).matches()),
                executor
        ).whenComplete((isValid, err) -> {
            if (err != null) {
                log.error("Error at validating text " + text + ":" + err.getMessage(), err);
                res.setResult(badRequest().body("internal error"));
            } else {
                res.setResult(ok(isValid ? "ok" : "not found"));
            }
        });
        return res;
    }

    public static void main(String[] args) throws Exception {
        log.info("Starting application...");
        SpringApplication.run(Application.class, args);
        log.info("Started application");
    }
}
