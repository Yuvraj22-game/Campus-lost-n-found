package com.example.campuslost_and_found.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

/**
 * Room Entity representing a Lost or Found item posted on campus.
 */
@Entity(tableName = "items")
public class ItemEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String type; // "LOST" or "FOUND"
    private String title;
    private String description;
    private String category;
    private String zone;
    private long dateMillis;
    private String imageUri;
    private String contactPhone;
    private String contactEmail;
    private boolean showPhonePublicly;
    private String verificationQuestion;
    private String verificationAnswer;
    private String status; // "ACTIVE" or "RESOLVED"
    private String postedByRoll;
    private long createdAt;

    // Default No-Arg Constructor for Room
    public ItemEntity() {
    }

    // Convenience Constructor for creating new items
    @Ignore
    public ItemEntity(String type, String title, String description, String category, String zone,
                      long dateMillis, String imageUri, String contactPhone, String contactEmail,
                      boolean showPhonePublicly, String verificationQuestion, String verificationAnswer,
                      String status, String postedByRoll, long createdAt) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.category = category;
        this.zone = zone;
        this.dateMillis = dateMillis;
        this.imageUri = imageUri;
        this.contactPhone = contactPhone;
        this.contactEmail = contactEmail;
        this.showPhonePublicly = showPhonePublicly;
        this.verificationQuestion = verificationQuestion;
        this.verificationAnswer = verificationAnswer;
        this.status = status;
        this.postedByRoll = postedByRoll;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public long getDateMillis() {
        return dateMillis;
    }

    public void setDateMillis(long dateMillis) {
        this.dateMillis = dateMillis;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public boolean isShowPhonePublicly() {
        return showPhonePublicly;
    }

    public void setShowPhonePublicly(boolean showPhonePublicly) {
        this.showPhonePublicly = showPhonePublicly;
    }

    public String getVerificationQuestion() {
        return verificationQuestion;
    }

    public void setVerificationQuestion(String verificationQuestion) {
        this.verificationQuestion = verificationQuestion;
    }

    public String getVerificationAnswer() {
        return verificationAnswer;
    }

    public void setVerificationAnswer(String verificationAnswer) {
        this.verificationAnswer = verificationAnswer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPostedByRoll() {
        return postedByRoll;
    }

    public void setPostedByRoll(String postedByRoll) {
        this.postedByRoll = postedByRoll;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemEntity item = (ItemEntity) o;
        return id == item.id &&
                dateMillis == item.dateMillis &&
                showPhonePublicly == item.showPhonePublicly &&
                createdAt == item.createdAt &&
                Objects.equals(type, item.type) &&
                Objects.equals(title, item.title) &&
                Objects.equals(description, item.description) &&
                Objects.equals(category, item.category) &&
                Objects.equals(zone, item.zone) &&
                Objects.equals(imageUri, item.imageUri) &&
                Objects.equals(contactPhone, item.contactPhone) &&
                Objects.equals(contactEmail, item.contactEmail) &&
                Objects.equals(verificationQuestion, item.verificationQuestion) &&
                Objects.equals(verificationAnswer, item.verificationAnswer) &&
                Objects.equals(status, item.status) &&
                Objects.equals(postedByRoll, item.postedByRoll);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, title, description, category, zone, dateMillis, imageUri,
                contactPhone, contactEmail, showPhonePublicly, verificationQuestion,
                verificationAnswer, status, postedByRoll, createdAt);
    }
}
