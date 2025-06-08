package models;

public class User {
    private String userId;
    private String username;
    private String password;
    private String role; // ADMIN, MANAGER, CUSTOMER_SERVICE, DRIVER
    private String name;
    private String contact;

    public User() {}

    public User(String userId, String username, String password,
                String role, String name, String contact) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.name = name;
        this.contact = contact;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    @Override
    public String toString() {
        return userId + "," + username + "," + password + "," +
                role + "," + name + "," + contact;
    }

    public static User fromString(String str) {
        String[] parts = str.split(",");
        if (parts.length < 6) return null;
        return new User(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
    }
}