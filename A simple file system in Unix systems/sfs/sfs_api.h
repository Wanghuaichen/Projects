#define DISK_FILE "sfs.out"

#define BLOCK_SIZE 512
#define MAX_BLOCKS 512
#define MAXFILENAME 20
#define MAX_EXTENSION 3
#define MAGIC_NUM 0xABCD0005
#define MAX_INODES 100
#define MAX_FILES MAX_INODES-1
#define ROOT_INDEX 0
#define ROOT_FD 0

#define SUPER_BLOCK_BASE 0
#define INODE_TABLE_BASE 2
#define ROOT_DIR_BASE 24
#define INONDE_MAP_BASE 34
#define BLOCK_BIT_MAP_BASE 35
#define SINGLE_PTR_NUM 12
#define PTR_SIZE (sizeof(int))
#define MAX_FILE_SIZE BLOCK_SIZE*SINGLE_PTR_NUM +BLOCK_SIZE*BLOCK_SIZE/PTR_SIZE


typedef struct super_block {
    int magic;
    int block_size;
    int fs_size;
    int inode_table_len;
    int root_dir_inode;
} super_block_t;

typedef struct block_ptr {
    int direct[SINGLE_PTR_NUM];
    int indirect;
} block_ptr_t;

typedef struct inode {
    int mode;
    int link_cnt;
    int uid;
    int gid;
    int size;
    block_ptr_t block_ptr;
} inode_t;


typedef struct dir_entry {
    char status;
    char name[MAXFILENAME];
    int inode_idx;
} dir_entry_t;


typedef struct fd_table {
    char status;
    int inode_idx;
    int rw_ptr;
} fd_table_t;

int mksfs(int fresh);
int sfs_getnextfilename(char *fname);
int sfs_getfilesize(const char* path);
int sfs_fopen(char *name);
int sfs_fclose(int fileID);
int sfs_fread(int fileID, char *buf, int length);
int sfs_fwrite(int fileID, const char *buf, int length);
int sfs_fseek(int fileID, int loc);
int sfs_remove(char *file);
