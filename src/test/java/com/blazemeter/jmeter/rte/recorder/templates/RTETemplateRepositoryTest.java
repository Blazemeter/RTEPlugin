package com.blazemeter.jmeter.rte.recorder.templates;

import static jodd.io.FileUtil.copyFile;

import com.blazemeter.jmeter.rte.recorder.RTETemplateRepository;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class RTETemplateRepositoryTest {

  private static String TEMPLATES_LIST_NAME = "templates.xml";
  private static String TEMPLATES_LIST_PATH = "/" + TEMPLATES_LIST_NAME;
  private static String TEMPLATE_NAME = "RteRecordingTemplate.jmx";
  private static String TEMPLATE_PATH = "/" + TEMPLATE_NAME;
  private static String TEMPLATE_DESCRIPTION_PATH = "/RteRecordingTemplateDescription.xml";

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

  private RTETemplateRepository rteTemplateRepository;

  @Before
  public void setup() throws IOException {
    rteTemplateRepository = new RTETemplateRepository(tempFolder.getRoot().getPath() + "/");

    URL templateListFile = getClass().getResource(TEMPLATES_LIST_NAME);
    copyFile(templateListFile.getPath(), tempFolder.getRoot().getPath() + TEMPLATES_LIST_PATH);
  }

  @Test
  public void shouldAddATemplateAndATemplateDescriptionWhenNotExistTheTemplate()
      throws IOException {
    rteTemplateRepository.addRTETemplate(TEMPLATE_NAME, TEMPLATE_PATH,
        TEMPLATE_DESCRIPTION_PATH, "Recording RTE");
    assertion();
  }

  private void assertion() throws IOException {
    File resultTemplate = new File(tempFolder.getRoot().getPath() + TEMPLATE_PATH);
    File resultTemplatesList = new File(tempFolder.getRoot().getPath() + TEMPLATES_LIST_PATH);

    softly.assertThat(FileUtils.readFileToString(resultTemplate, "utf-8"))
        .isEqualToNormalizingWhitespace(getFileFromResources(TEMPLATE_PATH));
    String EXPECTED_TEMPLATE_PATH = "/templatesRteRecordingTemplate.xml";
    softly.assertThat(FileUtils.readFileToString(resultTemplatesList, "utf-8"))
        .isEqualToNormalizingWhitespace(getFileFromResources(EXPECTED_TEMPLATE_PATH));
  }

  private String getFileFromResources(String fileName) throws IOException {
    InputStream inputStream = this.getClass().getResourceAsStream(fileName);
    return IOUtils.toString(inputStream, "UTF-8");
  }

  @Test
  public void shouldNotAddTemplateDescriptionWhenItWasAlreadyAdded() throws IOException {
    rteTemplateRepository.addRTETemplate(TEMPLATE_NAME, TEMPLATE_PATH,
        TEMPLATE_DESCRIPTION_PATH, "Recording RTE");
    assertion();
  }

  @Test
  public void shouldNotAddATemplateWhenItWasAlreadyAdded() throws IOException {
    rteTemplateRepository.addRTETemplate(TEMPLATE_NAME, TEMPLATE_PATH,
        TEMPLATE_DESCRIPTION_PATH, "Recording RTE");
    File result = new File(tempFolder.getRoot().getPath() + TEMPLATE_NAME);
    long lastModifiedExpected = result.lastModified();
    rteTemplateRepository.addRTETemplate(TEMPLATE_NAME, TEMPLATE_PATH,
        TEMPLATE_DESCRIPTION_PATH, "Recording RTE");
    result = new File(tempFolder.getRoot().getPath() + TEMPLATE_NAME);
    long lastModifiedResult = result.lastModified();
    softly.assertThat(lastModifiedResult == lastModifiedExpected);
    assertion();
  }

}
