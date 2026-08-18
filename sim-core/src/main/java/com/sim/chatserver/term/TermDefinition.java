package com.sim.chatserver.term;

public class TermDefinition {

    private final Long id;
    private final String name;
    private final String description;
    private final String matchPattern;
    private final String matchType;
    private final boolean systemFlag;

    @SuppressWarnings("unused")
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    @SuppressWarnings("unused")
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    TermDefinition(Long id, String name, String description, String matchPattern, String matchType, boolean systemFlag) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.matchPattern = matchPattern;
        this.matchType = matchType;
        this.systemFlag = systemFlag;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getMatchPattern() {
        return matchPattern;
    }

    public String getMatchType() {
        return matchType;
    }

    public boolean isSystemFlag() {
        return systemFlag;
    }
}
