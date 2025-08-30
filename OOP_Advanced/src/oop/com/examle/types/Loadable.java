package oop.com.examle.types;

// cargo reduces effective speed
public interface Loadable {
    void load(int kg);
    void unload(int kg);
    int getCargoKg();
}
