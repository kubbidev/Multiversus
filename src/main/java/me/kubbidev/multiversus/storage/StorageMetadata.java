package me.kubbidev.multiversus.storage;

public class StorageMetadata {

    // remote
    private Boolean connected;
    private Integer ping;

    // local
    private Long sizeBytes;

    public Boolean connected() {
        return this.connected;
    }

    public Integer ping() {
        return this.ping;
    }

    public Long sizeBytes() {
        return this.sizeBytes;
    }

    public StorageMetadata connected(boolean connected) {
        this.connected = connected;
        return this;
    }

    public StorageMetadata ping(int ping) {
        this.ping = ping;
        return this;
    }

    public StorageMetadata sizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
        return this;
    }

    public StorageMetadata combine(StorageMetadata other) {
        if (this.connected == null || (other.connected != null && !other.connected)) {
            this.connected = other.connected;
        }
        if (this.ping == null || (other.ping != null && other.ping > this.ping)) {
            this.ping = other.ping;
        }
        if (this.sizeBytes == null || (other.sizeBytes != null && other.sizeBytes > this.sizeBytes)) {
            this.sizeBytes = other.sizeBytes;
        }
        return this;
    }

}