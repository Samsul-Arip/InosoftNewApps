import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    private var isTestMode: Bool {
        ProcessInfo.processInfo.arguments.contains("-uitesting")
    }

    private var isOfflineTest: Bool {
        ProcessInfo.processInfo.arguments.contains("-uitest-offline")
    }

    func makeUIViewController(context: Self.Context) -> UIViewController {
        if isTestMode {
            return MainViewControllerKt.TestViewController(isOffline: isOfflineTest)
        }
        return MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
            .accessibilityElement(children: .contain)
    }
}