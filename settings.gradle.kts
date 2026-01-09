pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

// プロジェクト全体のリポジトリ（ライブラリの取得先）を一括管理する設定
dependencyResolutionManagement {
    // RepositoriesModeなどが @Incubating (実験的機能) とマークされているため
    // Android Studioで警告が出るが、現在のAndroid開発では標準的な記述のため無視して問題ない。
    // FAIL_ON_PROJECT_REPOS: 各モジュールでのリポジトリ個別定義を禁止し、ここの設定を強制する。
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Zubora Diary"
include(":app")
