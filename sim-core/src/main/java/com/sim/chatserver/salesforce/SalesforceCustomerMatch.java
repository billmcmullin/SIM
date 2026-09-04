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

    final String getContactId() {
        return contactId;
    }

    public String contactId() {
        return getContactId();
    }

    final void setContactId(String contactId) {
        this.contactId = contactId;
    }

    final String getAccountId() {
        return accountId;
    }

    public String accountId() {
        return getAccountId();
    }

    final void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    final String getName() {
        return name;
    }

    public String fullName() {
        return getName();
    }

    final void setName(String name) {
        this.name = name;
    }

    final String getEmail() {
        return email;
    }

    public String emailValue() {
        return getEmail();
    }

    final void setEmail(String email) {
        this.email = email;
    }

    final String getPhone() {
        return phone;
    }

    public String phoneValue() {
        return getPhone();
    }

    final void setPhone(String phone) {
        this.phone = phone;
    }

    final String getTitle() {
        return title;
    }

    public String titleValue() {
        return getTitle();
    }

    final void setTitle(String title) {
        this.title = title;
    }

    final String getDepartment() {
        return department;
    }

    public String departmentValue() {
        return getDepartment();
    }

    final void setDepartment(String department) {
        this.department = department;
    }

    final String getRawJson() {
        return rawJson;
    }

    public String rawJsonValue() {
        return getRawJson();
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
