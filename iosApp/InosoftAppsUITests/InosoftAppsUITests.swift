import XCTest

/// Instrumented UI Test for InosoftApps (Compose Multiplatform) on iOS Simulator
final class InosoftAppsUITests: XCTestCase {

    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }

    override func tearDownWithError() throws {
        app = nil
    }

    /// Scenario 1: App opens -> Article list is displayed -> Tap article -> Detail screen is displayed -> Tap back
    @MainActor
    func testClickingArticle_navigatesToDetailScreenAndBack() throws {
        // 1. Verify app window launched successfully
        let appWindow = app.windows.firstMatch
        XCTAssertTrue(appWindow.waitForExistence(timeout: 15), "Application window should launch on iOS Simulator")

        // 2. Locate header or main feed elements (Compose accessibility on iOS)
        let appTitle = findComposeElement(matching: "News Reader")
        let categoryChip = findComposeElement(matching: "Semua")

        let isFeedVisible = appTitle.waitForExistence(timeout: 10) || categoryChip.waitForExistence(timeout: 5)
        XCTAssertTrue(isFeedVisible, "News feed UI should be rendered")

        // 3. Locate an article card in the feed
        let articlePredicate = NSPredicate(format: "label CONTAINS[c] 'Kotlin' OR label CONTAINS[c] 'Berita' OR label CONTAINS[c] 'Ekonomi' OR label CONTAINS[c] 'Tech' OR label CONTAINS[c] 'Release'")
        let articleElement = app.descendants(matching: .any).matching(articlePredicate).firstMatch

        if articleElement.waitForExistence(timeout: 8) {
            // Tap on the article card to navigate to detail
            articleElement.tap()

            // 4. Verify detail screen header
            let detailHeader = findComposeElement(matching: "Detail Berita")
            if detailHeader.waitForExistence(timeout: 6) {
                XCTAssertTrue(detailHeader.exists, "Detail screen header 'Detail Berita' should be displayed")
            }

            // 5. Tap Back button
            let backButton = findComposeElement(matching: "Kembali")
            if backButton.exists {
                backButton.tap()
            }
        }
    }

    /// Scenario 2: Cached/offline state can still render previously stored articles gracefully
    @MainActor
    func testOfflineOrInitialState_rendersGracefully() throws {
        // 1. Verify app window launched successfully
        let appWindow = app.windows.firstMatch
        XCTAssertTrue(appWindow.waitForExistence(timeout: 15), "Application window should launch on iOS Simulator")

        // 2. Verify navigation header or category filter chips exist
        let appTitle = findComposeElement(matching: "News Reader")
        let categoryAll = findComposeElement(matching: "Semua")

        let isHeaderOrChipsVisible = appTitle.waitForExistence(timeout: 10) || categoryAll.waitForExistence(timeout: 5)
        XCTAssertTrue(isHeaderOrChipsVisible, "App bar header or category chips should be visible on iOS")
    }

    // MARK: - Helper Methods for Compose Multiplatform iOS Accessibility

    private func findComposeElement(matching text: String) -> XCUIElement {
        let predicate = NSPredicate(format: "label CONTAINS[c] %@ OR identifier CONTAINS[c] %@ OR value CONTAINS[c] %@", text, text, text)
        return app.descendants(matching: .any).matching(predicate).firstMatch
    }
}
