package com.example.taskmanager.e2e;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TaskE2EPlaywrightTest {

  private Playwright playwright;
  private Browser browser;
  private BrowserContext context;
  private Page page;
  private String baseUrl;

  @BeforeAll
  void beforeAll() {
    baseUrl = System.getenv("FRONTEND_URL");
    if (baseUrl == null || baseUrl.isBlank()) baseUrl = "http://localhost:5173";
    playwright = Playwright.create();
    browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
  }

  @AfterAll
  void afterAll() {
    if (context != null) context.close();
    if (browser != null) browser.close();
    if (playwright != null) playwright.close();
  }

  @BeforeEach
  void beforeEach() {
    context = browser.newContext();
    page = context.newPage();
  }

  @AfterEach
  void afterEach() {
    if (context != null) context.close();
  }

  @Test
  void createTaskAndChangeStatus() {
    page.navigate(baseUrl);

    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("New Task")).click();

    String title = "E2E Task " + DateTimeFormatter.ISO_INSTANT.format(Instant.now());
    page.getByLabel("Title").fill(title);
    page.getByLabel("Description").fill("Created by Playwright E2E test");
    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Create")).click();

    Locator row = page.locator("tr:has(td:has-text(\"" + title + "\"))");
    assertThat(row).isVisible();

    Locator select = row.locator("select");
    select.selectOption("IN_PROGRESS");
    assertThat(select).hasValue("IN_PROGRESS");
  }
}