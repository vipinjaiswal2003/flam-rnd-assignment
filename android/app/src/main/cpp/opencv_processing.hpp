#pragma once
#include <vector>

void process_rgba_frame(unsigned char* rgba,
                        int width,
                        int height,
                        std::vector<signed char>& out_gray);
