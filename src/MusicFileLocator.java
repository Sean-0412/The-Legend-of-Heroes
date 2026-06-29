/**
 * MusicFileLocator.java
 * 說明：尋找本機音樂檔案，優先使用不會上傳 GitHub 的 kiseki music 資料夾。
 */

package src;

import java.io.File;

final class MusicFileLocator {
    private static final String LOCAL_MUSIC_DIRECTORY = "kiseki music";
    private static final String LEGACY_RESOURCE_DIRECTORY = "resources";

    private MusicFileLocator() {
    }

    static File find(String fileName) {
        File localFile = new File(LOCAL_MUSIC_DIRECTORY, fileName);
        if (localFile.isFile()) {
            return localFile;
        }
        return new File(LEGACY_RESOURCE_DIRECTORY, fileName);
    }
}
