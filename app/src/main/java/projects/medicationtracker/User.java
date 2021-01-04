package projects.medicationtracker;

public class User
{
    private int userId;
    private String username;
    private String password;

    User (int id, String name, String pw)
    {
        userId = id;
        username = name;
        password = pw;
    }

    // Getters and setters
    public int getUserId()
    {
        return userId;
    }

    public void setUserId(int userId)
    {
        this.userId = userId;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
}
