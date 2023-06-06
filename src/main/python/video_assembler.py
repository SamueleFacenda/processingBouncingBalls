import cv2
import os


PATH = '..\\..\\..\\media'
EXT = '.png'
FRAMES_PATH = PATH + '\\frames'
VIDEO_PATH = PATH + '\\videos'

images = [img for img in os.listdir(FRAMES_PATH) if img.endswith(".png")]
frame = cv2.imread(os.path.join(FRAMES_PATH, images[0]))
height, width, layers = frame.shape

video = cv2.VideoWriter(VIDEO_PATH + '\\video1.mp4', cv2.VideoWriter_fourcc(*'MP4V'), 13, (width,height))

for image in images:
    video.write(cv2.imread(os.path.join(FRAMES_PATH, image)))

cv2.destroyAllWindows()
video.release()
