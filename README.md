# TV-Show-Episode-Picker

A program that randomly picks episodes of a TV show. I originally designed this program to pick episodes of Seinfeld, which is why the program icon is a picture of Jerry Seinfeld. However, TV shows are defined in a `.txt` file, and so any show can be used.

## File Format

Formatting the `.txt` file for any show is pretty simple. First, include every episode of the show that you may want to watch. Each episode goes on its own line. You can put every episode, or maybe exclude ones you don't like. After you've listed every episode you want, leave a blank line, and then the words "Cannot get:". Press enter one more time to add a blank line at the end, and then you're done. See the example file, which contains every Seinfeld episode, for more details.

## Program Usage

The main window has only three buttons: generate, load, and buffer. The generate and buffer buttons will not do anything until you load a file. A file must conform to the standards described above to be considered valid. Once a valid file is loaded, you can hit generate, and it will pick a random episode of the show. When an episode is chosen, it is added to the buffer, and you will not get it again unless it is removed from the buffer.

Under the buffer menu, you'll find that every episode is listed in the order you watched them. You can remove individual episodes from the buffer by clicking on them, or you can clear the entire buffer with the clear all button. Note that the buffer does not update while it's open. In other words, if you have the buffer window open, generating new episodes will not cause those episodes to appear in the buffer menu. You must close and reopen the buffer menu to refresh it at this time. The buffer is still *updated*, but the *display* is not.

When you generate an episode, it will tell you how many episodes are currently in the buffer, and how many total episodes you have included in the system.
