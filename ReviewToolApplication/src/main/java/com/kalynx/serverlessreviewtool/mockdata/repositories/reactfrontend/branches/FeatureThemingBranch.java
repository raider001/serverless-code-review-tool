package com.kalynx.serverlessreviewtool.mockdata.repositories.reactfrontend.branches;
import com.kalynx.serverlessreviewtool.mockdata.repositories.BaseRepository;
import java.nio.file.Files;
import java.nio.file.Path;
public class FeatureThemingBranch extends BaseRepository {
    public static void create(Path repoPath) throws Exception {
        createAndCheckoutBranch(repoPath, "feature/dark-mode-theme");
        Path themeFile = repoPath.resolve("src/theme.ts");
        Files.createDirectories(themeFile.getParent());
        Files.writeString(themeFile,
            "export interface Theme {\n" +
            "  colors: {\n" +
            "    background: string;\n" +
            "    foreground: string;\n" +
            "    primary: string;\n" +
            "  };\n" +
            "}\n" +
            "\n" +
            "export const darkTheme: Theme = {\n" +
            "  colors: {\n" +
            "    background: '#1e1e1e',\n" +
            "    foreground: '#ffffff',\n" +
            "    primary: '#007acc',\n" +
            "  },\n" +
            "};\n");
        commitFile(repoPath, "src/theme.ts", "feat: Add dark theme support");
        Files.writeString(themeFile,
            "export interface Theme {\n" +
            "  colors: {\n" +
            "    background: string;\n" +
            "    foreground: string;\n" +
            "    primary: string;\n" +
            "    secondary: string;\n" +
            "  };\n" +
            "}\n" +
            "\n" +
            "export const darkTheme: Theme = {\n" +
            "  colors: {\n" +
            "    background: '#1e1e1e',\n" +
            "    foreground: '#ffffff',\n" +
            "    primary: '#007acc',\n" +
            "    secondary: '#6c757d',\n" +
            "  },\n" +
            "};\n" +
            "\n" +
            "export const lightTheme: Theme = {\n" +
            "  colors: {\n" +
            "    background: '#ffffff',\n" +
            "    foreground: '#000000',\n" +
            "    primary: '#0066cc',\n" +
            "    secondary: '#6c757d',\n" +
            "  },\n" +
            "};\n");
        commitFile(repoPath, "src/theme.ts", "feat: Add light theme and secondary color");
        checkoutMain(repoPath);
    }
}
