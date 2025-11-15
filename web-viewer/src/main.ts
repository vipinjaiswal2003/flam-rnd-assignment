const img = document.getElementById("frameImage") as HTMLImageElement;
const overlay = document.getElementById("overlayText") as HTMLDivElement;

const IMAGE_PATH = "../sample-output/processed_frame.png";

function init() {
  img.src = IMAGE_PATH;
  img.onload = () => {
    const width = img.naturalWidth;
    const height = img.naturalHeight;
    const fps = 15; // dummy FPS for display
    overlay.textContent = `Resolution: ${width}x${height} | FPS: ${fps} | Source: Android OpenCV Canny`;
  };
  img.onerror = () => {
    overlay.textContent = "processed_frame.png not found. Export a frame from the Android app.";
  };
}

init();
