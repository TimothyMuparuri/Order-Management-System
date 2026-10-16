package com.oms.image.vision;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StubVisionProvider implements VisionProvider {
    @Override
    public List<ItemCandidate> matchProduct(ImageRef image) {
        return List.of(
                new ItemCandidate("sku-stub-" + image.imageId().substring(0, 8), 0.91),
                new ItemCandidate("sku-alt-" + image.imageId().substring(0, 8), 0.72)
        );
    }
}
