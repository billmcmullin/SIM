package com.sim.chatserver.salesforce;

public class SalesforceCustomerMatch {

    private String contactId;
    private String accountId;
    private String name;
    private String email;
    private String phone;
    private String title;
    private String department;
    private String rawJson;

    public String getContactId() {
        return contactId;
    }

    void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getAccountId() {
        return accountId;
    }

    void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    public String getDepartment() {
        return department;
    }

    void setDepartment(String department) {
        this.department = department;
    }

    public String getRawJson() {
        return rawJson;
    }

    void setRawJson(String rawJson) {
        this.rawJson = rawJson;
    }

    @Override
    public String toString() {
        return "SalesforceCustomerMatch{"
                + "contactId='" + contactId + '\''
                + ", accountId='" + accountId + '\''
                + ", name='" + name + '\''
                + ", email='" + email + '\''
                + ", phone='" + phone + '\''
                + ", title='" + title + '\''
                + ", department='" + department + '\''
                + '}';
    }
}
