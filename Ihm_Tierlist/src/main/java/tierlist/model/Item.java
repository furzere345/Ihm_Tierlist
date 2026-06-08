package tierlist.model;

import java.io.Serializable;
import java.util.UUID;

public class Item implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum ItemType { TEXT, IMAGE }

    private String id;
    private String label;
    private ItemType type;
    private byte[] imageData;
    private double size;

    //Constructeur texte
    public Item(String label) {
        this.id = UUID.randomUUID().toString();
        this.label = label;
        this.type = ItemType.TEXT;
        this.size = 80.0;
    }

    //Constructeur image
    public Item(String label, byte[] imageData) {
        this(label);
        this.imageData = imageData;
        this.type = ItemType.IMAGE;
    }

    public Item duplicate() {
        Item copy = new Item(this.label);
        copy.type = this.type;
        copy.imageData = this.imageData;
        copy.size = this.size;
        return copy;
    }

    public String getId() {
        return id;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    public ItemType getType() {
        return type;
    }
    public byte[] getImageData() {
        return imageData;
    }
    public void setImageData(byte[] data) {
        this.imageData = data; this.type = ItemType.IMAGE;
    }
    public double getSize() {
        return size;
    }
    public void setSize(double size) {
        this.size = size;
    }
}
