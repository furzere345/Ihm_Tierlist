package tierlist.service;

import tierlist.model.TierList;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class PersistenceService {

    //Dossier de sauvegarde dans le home de l'utilisateur
    private static final Path SAVE_DIR = Paths.get(
            System.getProperty("user.home"), ".tierlists"
    );

    public PersistenceService() {
        try {
            Files.createDirectories(SAVE_DIR);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Sauvegarder une tier-list
    public void save(TierList tl) {
        Path file = SAVE_DIR.resolve(tl.getId() + ".tl");
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(file.toFile()))) {
            oos.writeObject(tl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Charger toutes les tier-lists
    public List<TierList> loadAll() {
        List<TierList> result = new ArrayList<>();
        File[] files = SAVE_DIR.toFile().listFiles(
                (dir, name) -> name.endsWith(".tl")
        );
        if (files == null) return result;
        for (File f : files) {
            TierList tl = loadFrom(f);
            if (tl != null) result.add(tl);
        }
        return result;
    }

    //Supprimer une tier-list
    public void delete(String id) {
        Path file = SAVE_DIR.resolve(id + ".tl");
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Importer depuis un fichier choisi
    public TierList importFrom(File file) {
        return loadFrom(file);
    }

    //Exporter vers un fichier choisi
    public void exportTo(TierList tl, File destination) {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(destination))) {
            oos.writeObject(tl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Lecture interne
    private TierList loadFrom(File file) {
        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(file))) {
            return (TierList) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
