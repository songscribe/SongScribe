package songscribe.data;

public class SlurData {
    private int xPos1, xPos2, yPos1, yPos2, ctrly;

    public SlurData(int xPos1, int xPos2, int yPos1, int yPos2, int ctrly) {
        this.xPos1 = xPos1;
        this.xPos2 = xPos2;
        this.yPos1 = yPos1;
        this.yPos2 = yPos2;
        this.ctrly = ctrly;
    }

    public SlurData(String data) {
        if (data != null) {
            String[] split = data.split(",");
            xPos1 = Integer.parseInt(split[0]);
            yPos1 = Integer.parseInt(split[1]);
            xPos2 = Integer.parseInt(split[2]);
            yPos2 = Integer.parseInt(split[3]);
            ctrly = Integer.parseInt(split[4]);
        }
    }

    @Override
    public String toString() {
        return String.format("%d,%d,%d,%d,%d", xPos1, yPos1, xPos2, yPos2, ctrly);
    }

    public int getxPos1() {
        return xPos1;
    }

    public int getxPos2() {
        return xPos2;
    }

    public int getyPos1() {
        return yPos1;
    }

    public int getyPos2() {
        return yPos2;
    }

    public int getCtrly() {
        return ctrly;
    }

    public void setxPos1(int xPos1) {
        this.xPos1 = xPos1;
    }

    public void setxPos2(int xPos2) {
        this.xPos2 = xPos2;
    }

    public void setyPos1(int yPos1) {
        this.yPos1 = yPos1;
    }

    public void setyPos2(int yPos2) {
        this.yPos2 = yPos2;
    }

    public void setCtrly(int ctrly) {
        this.ctrly = ctrly;
    }
}
