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

    @SuppressWarnings("unused")
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    @SuppressWarnings("unused")
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    public String getContactId() {
        return contactId;
    }

    final void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getAccountId() {
        return accountId;
    }

    final void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getName() {
        return name;
    }

    final void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    final void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    final void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTitle() {
        return title;
    }

    final void setTitle(String title) {
        this.title = title;
    }

    public String getDepartment() {
        return department;
    }

    final void setDepartment(String department) {
        this.department = department;
    }

    public String getRawJson() {
        return rawJson;
    }

    final void setRawJson(String rawJson) {
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
