package com.oms.image.vision;

import java.util.List;

public interface VisionProvider {
    List<ItemCandidate> matchProduct(ImageRef image);

    record ImageRef(String imageId, String objectKey, String contentType) {}
    record ItemCandidate(String itemRef, double confidence) {}
}
