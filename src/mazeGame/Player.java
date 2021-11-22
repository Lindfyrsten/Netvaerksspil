package mazeGame;

public class Player {

    private String name;
    private int xpos, ypos, kills, deaths;
    private Direction direction;
    boolean shooting = false;
    boolean moving = false;
    private int ammo;
    private int bombs;
    private int bombSize;
    private boolean dead;

    public Player(String name, int xpos, int ypos, Direction direction) {

        this.name = name;
        this.xpos = xpos;
        this.ypos = ypos;
        this.direction = direction;
        dead = true;
        ammo = 0;
        bombs = 1;
        bombSize = 1;
        kills = 0;
        deaths = 0;
    }

    public int getXpos() {

        return xpos;
    }

    public void setXpos(int xpos) {

        this.xpos = xpos;
    }

    public int getYpos() {

        return ypos;
    }

    public void setYpos(int ypos) {

        this.ypos = ypos;
    }

    public Direction getDirection() {

        return direction;
    }

    public void setDirection(Direction direction) {

        this.direction = direction;
    }
    
    public String getName() {

        return name;
    }

    public boolean isShooting() {

        return shooting;
    }

    public void setShooting(boolean shooting) {

        this.shooting = shooting;
    }

    public boolean isMoving() {

        return moving;
    }

    public void setMoving(boolean moving) {

        this.moving = moving;
    }

    public int getAmmo() {

        return ammo;
    }

    public void setAmmo(int ammo) {

        this.ammo = ammo;
    }

    public int getBombs() {

        return bombs;
    }

    public void setBombs(int bombs) {

        this.bombs = bombs;
    }

    public int getBombSize() {

        return bombSize;
    }

    public void setBombSize(int bombSize) {

        this.bombSize = bombSize;
    }

    public void setDead(boolean dead) {
        
        this.dead = dead;
    }

    public void addDeath() {
        
        deaths++;
    }

    public boolean isDead() {

        return dead;
    }

    public int getDeaths() {
        
        return deaths;
    }

    public void addKill(int kills) {

        this.kills += kills;
    }

    public int getKills() {

        return kills;
    }
}
