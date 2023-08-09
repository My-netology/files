import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Main {
    private static void createCatalog(String directoryName, StringBuilder log) {
        File ioDevice = new File(directoryName);
        if (ioDevice.mkdir()) {
            log.append("Directory ").append(directoryName).append(" created successfully.\n");
        } else {
            if (ioDevice.exists()) {
                log.append("Directory ").append(directoryName).append(" already exists.\n");
            } else {
                log.append("Error when creating directory ").append(directoryName).append(".\n");
            }
        }
    }

    private static void createFile(String fileName, StringBuilder log) {
        File ioDevice = new File(fileName);
        try {
            if (ioDevice.createNewFile()) {
                log.append("File ").append(fileName).append(" created successfully.\n");
            } else {
                if (ioDevice.exists()) {
                    log.append("File ").append(fileName).append(" already exists.\n");
                } else {
                    log.append("Error when creating file ").append(fileName).append(".\n");
                }
            }
        } catch (IOException e) {
            log.append("Error when creating file ").append(fileName).append(".\n");
            System.out.println(e.getMessage());
        }
    }

    private static void saveGame(GameProgress game, String fileName) {
        try (ObjectOutputStream ioObjStream = new ObjectOutputStream(new FileOutputStream(fileName))) {
            ioObjStream.writeObject(game);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void deleteFiles(String... filesNames) {
        for(String fileName : filesNames) {
            File game = new File(fileName);
            game.delete();
        }
    }

    private static void zipFiles(String archiveName, String... filesNames) {
        try (ZipOutputStream ioArchiveDevice = new ZipOutputStream(new FileOutputStream(archiveName))) {
            for (String fileName : filesNames) {
                try (FileInputStream ioDevice = new FileInputStream(fileName)) {
                    File game = new File(fileName);
                    ZipEntry entry = new ZipEntry(game.getName());
                    ioArchiveDevice.putNextEntry(entry);
                    byte[] buffer = new byte[ioDevice.available()];
                    int size = ioDevice.read(buffer);
                    System.out.printf("File: %s => Size: %d\n", game.getName(), size);
                    ioArchiveDevice.write(buffer);
                    ioArchiveDevice.closeEntry();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void openZip(String archiveName, String folderName) {
        try (ZipInputStream ioArchiveDevice = new ZipInputStream(new FileInputStream(archiveName))) {
            ZipEntry entry;
            String fileName;
            while ((entry = ioArchiveDevice.getNextEntry()) != null) {
                fileName = folderName + '/' + entry.getName();
                FileOutputStream game = new FileOutputStream(fileName);
                for (int c = ioArchiveDevice.read(); c != -1; c = ioArchiveDevice.read()) {
                    game.write(c);
                }
                game.flush();
                ioArchiveDevice.closeEntry();
                game.close();
                openProgress(fileName);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void openProgress(String fileName) {
        try (ObjectInputStream ioObjDevice = new ObjectInputStream(new FileInputStream(fileName))) {
            GameProgress game = (GameProgress) ioObjDevice.readObject();
            System.out.println(game.toString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Установка
        String instDir = "/home/khrapatiy/Games";
        StringBuilder log = new StringBuilder();
        createCatalog(instDir + "/src", log);
        createCatalog(instDir + "/res", log);
        createCatalog(instDir + "/savegames", log);
        createCatalog(instDir + "/temp", log);
        createCatalog(instDir + "/src/main", log);
        createCatalog(instDir + "/src/test", log);
        createFile(instDir + "/src/main/Main.java", log);
        createFile(instDir + "/src/main/Utils.java", log);
        createCatalog(instDir + "/res/drawables", log);
        createCatalog(instDir + "/res/vectors", log);
        createCatalog(instDir + "/res/icons", log);
        createFile(instDir + "/temp/temp.txt", log);
        try (FileWriter logFile = new FileWriter(instDir + "/temp/temp.txt")) {
            logFile.write(log.toString());
            logFile.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        // Сохранение
        GameProgress game1 = new GameProgress(100, 23, 6, 235.5);
        String file1 = instDir + "/savegames/game1.dat";
        saveGame(game1, file1);
        GameProgress game2 = new GameProgress(93, 23, 7, 265.7);
        String file2 = instDir + "/savegames/game2.dat";
        saveGame(game2, file2);
        GameProgress game3 = new GameProgress(100, 25, 7, 295.5);
        String file3 = instDir + "/savegames/game3.dat";
        saveGame(game3, file3);
        String zip = instDir + "/savegames/games.zip";
        zipFiles(zip, file1, file2, file3);
        deleteFiles(file1, file2, file3);
        String folder = instDir + "/savegames";
        // Загрузка*
        openZip(zip, folder);
    }
}