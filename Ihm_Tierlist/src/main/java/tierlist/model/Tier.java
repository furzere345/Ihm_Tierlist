package tierlist.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Tier implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String colorHex;
    private double height;
    private int order;
    private List<Item> items;

    public Tier(String name, String colorHex) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.colorHex = colorHex.startsWith("#") ? colorHex : "#" + colorHex;
        this.height = 100.0;
        this.items = new ArrayList<>();
    }

    public Tier duplicate() {
        Tier copy = new Tier(this.name, this.colorHex);
        copy.height = this.height;
        copy.order = this.order;
        for (Item item : this.items) {
            copy.items.add(item.duplicate());
        }
        return copy;
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getColorHex() {
        return colorHex;
    }
    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }
    public double getHeight() {
        return height;
    }
    public void setHeight(double height) {
        this.height = height;
    }
    public int getOrder() {
        return order;
    }
    public void setOrder(int order) {
        this.order = order;
    }
    public List<Item> getItems() {
        return items;
    }
}
