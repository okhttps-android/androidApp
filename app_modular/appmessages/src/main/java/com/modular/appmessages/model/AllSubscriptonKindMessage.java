package com.modular.appmessages.model;

import com.core.model.SubscriptionNumber;

import java.util.List;

/**
 * 所有订阅类实体类
 * Created by RaoMeng on 2016/9/7.
 */
public class AllSubscriptonKindMessage {
    private String subscriptionKind;
    private List<SubscriptionNumber> subscriptionNumbers;


    public String getSubscriptionKind() {
        return subscriptionKind;
    }

    public void setSubscriptionKind(String subscriptionKind) {
        this.subscriptionKind = subscriptionKind;
    }

    public List<SubscriptionNumber> getSubscriptionNumbers() {
        return subscriptionNumbers;
    }

    public void setSubscriptionNumbers(List<SubscriptionNumber> subscriptionNumbers) {
        this.subscriptionNumbers = subscriptionNumbers;
    }
}
