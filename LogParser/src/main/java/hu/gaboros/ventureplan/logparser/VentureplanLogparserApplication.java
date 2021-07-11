package hu.gaboros.ventureplan.logparser;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.gaboros.ventureplan.logparser.model.MissionReport;
import hu.gaboros.ventureplan.logparser.service.MissionService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
public class VentureplanLogparserApplication {

  @Value("${ventureplan.log_folder}")
  private String logFolder;

  public static void main(String[] args) {
    SpringApplication.run(VentureplanLogparserApplication.class, args);
  }

  @Bean
  public CommandLineRunner runner(MissionService missionService) {
    return args -> {
      ObjectMapper mapper = new ObjectMapper();

      final File folder = new File(logFolder);

      int parsedLogs = 0;
      int newLogs = 0;
      long startTime = System.currentTimeMillis();
      for (final File fileEntry : folder.listFiles()) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileEntry))) {
          String mission;
          while ((mission = br.readLine()) != null) {
            try {
              MissionReport missionReport = mapper.readValue(mission, new TypeReference<>() {});
              boolean newLogCreated = missionService.save(missionReport, mission);
              parsedLogs++;
              if (newLogCreated) {
                newLogs++;
              }
            } catch (JsonParseException jsonParseException) {
              // skip, probably just old format
            }
          }
        }
      }

      long milliseconds = System.currentTimeMillis() - startTime;
      long minutes = (milliseconds / 1000) / 60;
      long seconds = (milliseconds / 1000) % 60;

      log.info("Number of logs parsed: {}", parsedLogs);
      log.info("Number of logs created: {}", newLogs);
      log.info(
          "Elapsed time: {}:{}",
          StringUtils.leftPad(String.valueOf(minutes), 2, "0"),
          StringUtils.leftPad(String.valueOf(seconds), 2, "0"));
    };
  }
}
