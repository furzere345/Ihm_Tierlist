package tierlist.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TierList implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String description;
    private List<Tier> tiers;
    private List<Item> unclassifiedItems;
    private byte[] coverImageData;

    public TierList(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = "";
        this.tiers = new ArrayList<>();
        this.unclassifiedItems = new ArrayList<>();
    }

    //Dupliquer une tier-list (nouveau id, même contenu)
    public TierList duplicate() {
        TierList copy = new TierList("Copie de " + this.name);
        copy.description = this.description;
        copy.coverImageData = this.coverImageData;
        for (Tier t : this.tiers) {
            copy.tiers.add(t.duplicate());
        }
        for (Item item : this.unclassifiedItems) {
            copy.unclassifiedItems.add(item.duplicate());
        }
        return copy;
    }

    //Reinitialiser : remettre tous les items dans unclassifiedItems
    public void reset() {
        for (Tier t : tiers) {
            unclassifiedItems.addAll(t.getItems());
            t.getItems().clear();
        }
    }

    //Getters / Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public List<Tier> getTiers() { return tiers; }
    public void setTiers(List<Tier> tiers) { this.tiers = tiers; }
    public List<Item> getUnclassifiedItems() { return unclassifiedItems; }
    public byte[] getCoverImageData() { return coverImageData; }
    public void setCoverImageData(byte[] data) { this.coverImageData = data; }
}
