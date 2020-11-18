FROM ubuntu:20.10

# Scripts and configuration
COPY files/root/* /root/
COPY files/bin/* /bin/

# Make sure line endings are Unix
# This changes nothing if core.autocrlf is set to input
RUN sed -i 's/\r$//' /root/.bashrc

RUN apt-get update && apt-get install -y \
    clang \
    clang-tidy \
    clang-format \
    g++ \
    make \
    valgrind \
    gdb \
    llvm \
    libgtest-dev \
    cmake

# clangd for vscode extension
RUN apt-get update && apt-get install -y \
    clangd-11

# make clangd as default
RUN update-alternatives --install /usr/bin/clangd clangd /usr/bin/clangd-11 100

# workaround for stropts.h
# exists in ubuntu:18.04:libc6-dev:/usr/include/stropts.h
# exists in ubuntu:20.10:musl-dev:/usr/include/x86_64-linux-musl/stropts.h
RUN apt-get update && apt-get install -y \
    musl-dev

RUN cp /usr/include/x86_64-linux-musl/stropts.h /usr/include/.

# GTEST installation for labs
WORKDIR /usr/src/gtest
RUN cmake CMakeLists.txt
RUN make
RUN cp lib/*.a /usr/lib
RUN mkdir -p /usr/local/lib/gtest/
RUN ln -s /usr/lib/libgtest.a /usr/local/lib/gtest/libgtest.a
RUN ln -s /usr/lib/libgtest_main.a /usr/local/lib/gtest/libgtest_main.a

# Grading
RUN apt-get update && apt-get install -y \
    git \
    acl \
    python3.9 \
    python3.9-dev \
    python3-pip \
    python3-xmltodict
RUN python3.9 -m pip install curricula curricula-shell

# Misc
RUN apt-get update && apt-get install -y \
    nano \
    vim \
    curl \
    zsh

RUN sh -c "$(curl -fsSL https://raw.github.com/ohmyzsh/ohmyzsh/master/tools/install.sh)" "" --unattended
RUN git clone --depth=1 https://github.com/romkatv/powerlevel10k.git ${ZSH_CUSTOM:-$HOME/.oh-my-zsh/custom}/themes/powerlevel10k
RUN chsh -s $(which zsh)

# re-copy ~/.zshrc after installation of ohmyzsh & powerlevel10k which modified it
COPY files/root/.zshrc /root/

# Make sure line endings are Unix
# This changes nothing if core.autocrlf is set to input
RUN sed -i 's/\r$//' /root/.zshrc

VOLUME ["/work"]
WORKDIR /work
