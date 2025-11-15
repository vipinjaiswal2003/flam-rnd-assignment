#include "opencv_processing.hpp"
#include <opencv2/imgproc.hpp>
#include <opencv2/core.hpp>
#include <cstring>

void process_rgba_frame(unsigned char* rgba,
                        int width,
                        int height,
                        std::vector<signed char>& out_gray) {

    cv::Mat rgbaMat(height, width, CV_8UC4, rgba);
    cv::Mat grayMat;
    cv::Mat edgesMat;

    cv::cvtColor(rgbaMat, grayMat, cv::COLOR_RGBA2GRAY);
    cv::Canny(grayMat, edgesMat, 80, 150);

    out_gray.resize(width * height);
    std::memcpy(out_gray.data(), edgesMat.data, width * height);
}
