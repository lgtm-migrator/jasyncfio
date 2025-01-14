package one.jasyncfio;

import java.util.Locale;

class Native {

    static {
        try {
            String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            if (!os.contains("linux")) {
                throw new RuntimeException("only supported on linux");
            }
            System.load(Utils.loadLib("libjasyncfio.so").toPath().toAbsolutePath().toString());
            String kernelVersion = Native.kernelVersion();
            if (!Native.checkKernelVersion(kernelVersion)) {
                throw new UnsupportedOperationException("you need at least kernel version 5.11, current version is: " + kernelVersion);
            }
        } catch (Throwable ex) {
            throw (Error) new UnsatisfiedLinkError("can't load native library").initCause(ex);
        }
    }

    static int ioUringEnter(int ringFd, int toSubmit, int minComplete, int flags) {
        int ret = ioUringEnter0(ringFd, toSubmit, minComplete, flags);
        if (ret < 0) {
            throw new RuntimeException("io_uring enter error: " + ret);
        }
        return ret;
    }

    public static Uring setupIoUring(int entries, int flags, int sqThreadIdle, int sqThreadCpu, int cqSize, int attachWqRingFd) {
        long[][] pointers = setupIoUring0(entries, flags, sqThreadIdle, sqThreadCpu, cqSize, attachWqRingFd);
        final SubmissionQueue submissionQueue = new SubmissionQueue(
                pointers[0][0],
                pointers[0][1],
                pointers[0][2],
                pointers[0][3],
                pointers[0][4],
                pointers[0][5],
                pointers[0][6],
                pointers[0][7],
                (int) pointers[0][8],
                pointers[0][9],
                (int) pointers[0][10],
                pointers[0][11]
        );
        final CompletionQueue completionQueue = new CompletionQueue(
                pointers[1][0],
                pointers[1][1],
                pointers[1][2],
                pointers[1][3],
                pointers[1][4],
                pointers[1][5],
                pointers[1][6],
                (int) pointers[1][7],
                (int) pointers[1][8],
                pointers[1][9]
        );
        return new Uring(completionQueue, submissionQueue, (int) pointers[0][10]);
    }

    private static native int ioUringEnter0(int ringFd, int toSubmit, int minComplete, int flags);

    private static native long[][] setupIoUring0(int entries, int flags, int sqThreadIdle, int sqThreadCpu, int cqSize, int attachWqRingFd);

    public static native int getEventFd();

    public static native int eventFdWrite(int fd, long value);

    static native long getDirectBufferAddress(java.nio.Buffer buffer);

    static native long getStringPointer(String str);

    static native void releaseString(String str, long ptr);

    static native String kernelVersion();

    public static native String decodeErrno(int errorCode);

    public static native void ioUringRegister(int fd, int opcode, long argPtr, int nrArgs);

    public static native long getFileSize(int fd);

    public static native long getPageSize();

    public static native void closeRing(int ringFd, long sqRingPtr, int sqRingSize, long cqRingPtr, int cqRingSize);

    public static native long probeBufferSize();

    public static native long probeOpSize();

    public static native long ioUringBufSize();

    public static native long ioUringBufRegSize();

    /**
     * took from netty io uring project
     */
    static boolean checkKernelVersion(String kernelVersion) {
        String[] versionComponents = kernelVersion.split("\\.");
        if (versionComponents.length < 3) {
            return false;
        }

        int major;
        try {
            major = Integer.parseInt(versionComponents[0]);
        } catch (NumberFormatException e) {
            return false;
        }

        if (major <= 4) {
            return false;
        }
        if (major > 5) {
            return true;
        }

        int minor;
        try {
            minor = Integer.parseInt(versionComponents[1]);
        } catch (NumberFormatException e) {
            return false;
        }

        return minor >= 11;
    }


    public static final byte IORING_OP_READ = UringConstants.ioRingOpRead();
    public static final byte IORING_OP_WRITE = UringConstants.ioRingOpWrite();
    public static final byte IORING_OP_CLOSE = UringConstants.ioRingOpClose();
    public static final byte IORING_OP_OPENAT = UringConstants.ioRingOpenAt();
    public static final byte IORING_OP_NOP = UringConstants.ioRingOpNop();
    public static final byte IORING_OP_STATX = UringConstants.ioRingOpStatx();
    public static final byte IORING_OP_FSYNC = UringConstants.ioRingOpFsync();
    public static final byte IORING_OP_FALLOCATE = UringConstants.ioRingOpFallocate();
    public static final byte IORING_OP_UNLINKAT = UringConstants.ioRingOpUnlinkAt();
    public static final byte IORING_OP_RENAMEAT = UringConstants.ioRingOpRenameAt();
    public static final byte IORING_OP_READV = UringConstants.ioRingOpReadv();
    public static final byte IORING_OP_WRITEV = UringConstants.ioRingOpWritev();
    public static final byte IORING_OP_READ_FIXED = UringConstants.ioRingOpReadFixed();
    public static final byte IORING_OP_WRITE_FIXED = UringConstants.ioRingOpWriteFixed();
    public static final byte IORING_OP_CONNECT = UringConstants.ioRingOpConnect();
    public static final byte IORING_OP_POLL_ADD = UringConstants.ioRingOpPollAdd();
    public static final byte IORING_OP_POLL_REMOVE = UringConstants.ioRingOpPollRemove();
    public static final byte IORING_OP_ACCEPT = UringConstants.ioRingOpAccept();
    public static final byte IORING_OP_TIMEOUT = UringConstants.ioRingOpTimeout();
    public static final byte IORING_OP_TIMEOUT_REMOVE = UringConstants.ioRingOpTimeoutRemove();
    public static final byte IORING_OP_SENDMSG = UringConstants.ioRingOpSendMsg();
    public static final byte IORING_OP_RECVMSG = UringConstants.ioRingOpRecvMsg();
    public static final byte IORING_OP_SEND = UringConstants.ioRingOpSend();
    public static final byte IORING_OP_RECV = UringConstants.ioRingOpRecv();
    public static final byte IORING_OP_SHUTDOWN = UringConstants.ioRingOpShutdown();
    public static final byte IORING_OP_SEND_ZC = UringConstants.ioRingOpSendZc();
    public static final byte IORING_OP_SPLICE = UringConstants.ioRingOpSplice();

    public static final int IORING_REGISTER_BUFFERS = UringConstants.ioRingRegisterBuffers();
    public static final int IORING_UNREGISTER_BUFFERS = UringConstants.ioRingUnregisterBuffers();
    public static final int IORING_REGISTER_FILES = UringConstants.ioRingRegisterFiles();
    public static final int IORING_UNREGISTER_FILES = UringConstants.ioRingUnregisterFiles();
    public static final int IORING_REGISTER_PBUF_RING = UringConstants.ioRingRegisterPbufRing();
    public static final int IORING_UNREGISTER_PBUF_RING = UringConstants.ioRingUnregisterPbufRing();
    public static final int IORING_REGISTER_PROBE = UringConstants.ioRingRegisterProbe();
    public static final int IORING_ENTER_GETEVENTS = UringConstants.ioRingEnterGetEvents();
    public static final int IORING_ENTER_SQ_WAKEUP = UringConstants.ioRingEnterSqWakeup();
    public static final int IORING_SQ_NEED_WAKEUP = UringConstants.ioRingSqNeedWakeup();
    public static final int IORING_SQ_CQ_OVERFLOW = UringConstants.ioRingSqCqOverflow();
    public static final int IORING_FSYNC_DATASYNC = UringConstants.ioRingFsyncDatasync();
    public static final int IORING_SETUP_SQPOLL = UringConstants.ioRingSetupSqPoll();
    public static final int IORING_SETUP_IOPOLL = UringConstants.ioRingSetupIoPoll();
    public static final int IORING_SETUP_SQ_AFF = UringConstants.ioRingSetupSqAff();
    public static final int IORING_SETUP_CQ_SIZE = UringConstants.ioRingSetupCqSize();
    public static final int IORING_SETUP_CLAMP = UringConstants.ioRingSetupClamp();
    public static final int IORING_SETUP_ATTACH_WQ = UringConstants.ioRingSetupAttachWq();
    public static final int IOSQE_BUFFER_SELECT = UringConstants.iosqeBufferSelect();

    public static final int IORING_CQE_F_BUFFER = UringConstants.ioRingCqeFBuffer();

    public static final int O_RDONLY = FileIoConstants.oRdOnly();
    public static final int O_WRONLY = FileIoConstants.oWrOnly();
    public static final int O_RDWR = FileIoConstants.oRdWr();
    public static final int O_TRUNC = FileIoConstants.oTrunc();
    public static final int O_CREAT = FileIoConstants.oCreat();
    public static final int STATX_SIZE = FileIoConstants.statxSize();
    public static final int O_DIRECT = FileIoConstants.oDirect();
    public static final int O_CLOEXEC = FileIoConstants.oCloexec();
    public static final int O_APPEND = FileIoConstants.oAppend();
    public static final int O_DSYNC = FileIoConstants.oDsync();
    public static final int O_EXCL = FileIoConstants.oExcl();
    public static final int O_NOATIME = FileIoConstants.oNoAtime();
    public static final int O_SYNC = FileIoConstants.oSync();
    public static final int POLLIN = FileIoConstants.pollin();
    public static final int SPLICE_F_MOVE = FileIoConstants.spliceFMove();
    public static final int SPLICE_F_NONBLOCK = FileIoConstants.spliceFNonblock();
    public static final int SPLICE_F_MORE = FileIoConstants.spliceFMore();
}
