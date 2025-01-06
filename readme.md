To test functionality as of 1/6/24, cd to this project's directory in your terminal and run the following command:
```
node-v22.12.0-win-x64/node.exe screenshot.js
```

This will generate a screenshot of the website at the URL specified in the `screenshot.js` file. The screenshot will be saved in the `src.test.images` directory.

Then you can paste the below code into a separate python project to actually see the animation. 

```python

import tkinter as tk
from PIL import Image, ImageTk
import os

# Directory where the screenshots are saved
screenshots_dir = r"D:\Projects\Java\Minecraft_Webpage_Viewer\src\test\images"
update_interval = 100  # Milliseconds


class ScreenshotViewer:
    def __init__(self, root):
        self.root = root
        self.root.title("Screenshot Viewer")
        self.label = tk.Label(root)
        self.label.pack()
        self.current_image = None
        self.update_image()

    def update_image(self):
        try:
            # Find the latest screenshot in the directory
            screenshots = [f for f in os.listdir(screenshots_dir) if f.endswith(".png")]
            if screenshots:
                latest_screenshot = max(
                    [os.path.join(screenshots_dir, f) for f in screenshots],
                    key=os.path.getmtime
                )
                # Open and display the latest screenshot
                img = Image.open(latest_screenshot)
                self.current_image = ImageTk.PhotoImage(img)
                self.label.config(image=self.current_image)
        except Exception as e:
            print(f"Error updating image: {e}")

        # Schedule the next update
        self.root.after(update_interval, self.update_image)


if __name__ == "__main__":
    root = tk.Tk()
    viewer = ScreenshotViewer(root)
    root.mainloop()

Ensure tkinter and pillow are installed by running `pip install tk pillow` in your terminal.
You will also probably need to replace the `screenshots_dir` variable with the path to the directory where the screenshots are saved on your system; in src\test\images
directory relative to this project's directory.