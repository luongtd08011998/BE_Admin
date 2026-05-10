package vn.hoidanit.springrestwithai.qlkh.dto;

public class DebtReminderResponse {
    private int sentCount;
    private int skipCount;

    public DebtReminderResponse() {}

    public DebtReminderResponse(int sentCount, int skipCount) {
        this.sentCount = sentCount;
        this.skipCount = skipCount;
    }

    public int getSentCount() {
        return sentCount;
    }

    public void setSentCount(int sentCount) {
        this.sentCount = sentCount;
    }

    public int getSkipCount() {
        return skipCount;
    }

    public void setSkipCount(int skipCount) {
        this.skipCount = skipCount;
    }
}
