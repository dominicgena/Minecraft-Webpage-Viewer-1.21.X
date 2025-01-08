JUST A WARNING - 

To test functionality as of 1/7/24, open this project in an IDE that has the Minecraft Development plugin installed.  This
will allow you to open the mod in a Minecraft Development environment.  You can then run the following command in the
Put the python script below in to your preferred python IDE, and run it, refactoring the path at the top to the path of the
screenshots directory.  Running for the first time will put the frames into the run/config/mapcast/framecontainer/profile0/frames directory.  
Each time after into the single-incremented index version of that directory (profile1, profile2, etc).  The frames will be saved as .png files.
If you want to see both tests, you'll need to create a copy of the main python script, refactoring the path again. 

Here's the commands you can use to start the web rendering process, containing my recommended web pages to use as the link argument, 
as they meet all the necessary qualifications.  
There will be hints in the console that display the expected argument types and/or acceptable values
1. /mapcast add j 13.13 15 15 "https://www.w3schools.com/howto/howto_css_loader.asp"
2. /mapcast add k 13.13 15 15 "https://cssloaders.github.io/"
3. /mapcast load j {x} {y} {z} {facing}
4. /mapcast load k {x} {y} {z} {facing}

The first two will generate a json file with the necessary parameters to render the webpage.  The last two will load the json file
and render the webpage. 
The script below can be used for monitoring. 

```


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
```
Ensure tkinter and pillow are installed by running `pip install tk pillow` in your terminal.
You will also probably need to replace the `screenshots_dir` variable with the path to the 
directory where the screenshots are saved on your system; in src\test\images
directory relative to this project's directory.

You may wonder what this has to do with displaying a map of the minecraft world.  
Well, there are various plugins that can be used to generate a map of the minecraft world and display
it as a webpage.  This code can be used to display the map in a window on your desktop. 
The map will update every 100 milliseconds.  You can adjust the update interval by changing
the `update_interval` variable. 

JSON files will soon contain a variable for the URL of the webpage, which will be passed as an argument in the /mapcast add command.

This readme may contain out-of-date information, the most accurate representation of progreass and functionality will be the commit messages.  

If there ends up being enough contributors, I'll make a discord server.  For now, you can contact me via LinkedIn: https://www.linkedin.com/in/dominic-gena-7b176232b/
Dominic Gena