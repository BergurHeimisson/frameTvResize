from setuptools import setup, find_packages

setup(
    name="frame-tv-resize",
    version="1.0.0",
    description="Resize images to Samsung Pro Frame TV dimensions (3840x2160)",
    author="Your Name",
    python_requires=">=3.7",
    py_modules=["frame_resize"],
    install_requires=[
        "Pillow>=10.0.0",
    ],
    entry_points={
        "console_scripts": [
            "frame-resize=frame_resize:main",
        ],
    },
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: MIT License",
        "Operating System :: OS Independent",
    ],
)
