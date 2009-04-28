package songscribe.data;

public class MyBorder {
    private int top, bottom, left, right;

    public MyBorder() {
    }

    public MyBorder(int size) {
        this.top = size;
        this.bottom = size;
        this.left = size;
        this.right = size;
    }

    public MyBorder(int horizontal, int vertical) {
        this.top = vertical;
        this.bottom = vertical;
        this.left = horizontal;
        this.right = horizontal;
    }

    public MyBorder(int top, int bottom, int left, int right) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
    }

    public int getTop() {
        return top;
    }

    public int getBottom() {
        return bottom;
    }

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getWidth(){
        return left+right;
    }

    public int getHeight(){
        return top+bottom;
    }
}
