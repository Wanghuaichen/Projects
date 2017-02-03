#include <strings.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>
#include "disk_emu.h"
#include "sfs_api.h"

#define num_blocks (MAX_INODES*sizeof(inode_t)/512) + 1
#define dir_blocks (MAX_INODES - 1)*sizeof(dir_entry_t)/512 + 1

super_block_t super;
dir_entry_t root_dir[MAX_INODES -1];
inode_t inode_table[MAX_INODES];
fd_table_t fd_table[MAX_FILES];

char free_inodes[BLOCK_SIZE];
char free_blocks[BLOCK_SIZE];

int sfs_getfilesize(const char* path) {
    int inode_idx;
    
    inode_idx = get_inode_from_name(path);
    if(inode_idx == -1){
        printf("no file named: %s being found\n",path);
        return -1;
    }
    
    return inode_table[inode_idx].size;
}


int currentID = 0;
int sfs_getnextfilename(char *fname) {
    int i;
    int num_files = 0;

    for(i = 0; i < MAX_INODES - 1; i++) {
        if(root_dir[i].status==0) {
            num_files++;
        }
    }
        
    while(currentID < num_files) {
        strcpy(fname, root_dir[currentID].name);
        currentID++;
        return 1;
    }
    
    // printf("reach the end of the file, return the pointer\n");
    currentID = 0;
    return 0;
}



int sfs_fopen(char *name) {    
    inode_t* inode;
    dir_entry_t new_file;
    
    if(strlen(name) > MAXFILENAME) {
        printf("invalid name: MAX_length reached\n");
        return -1;
    } else if(strchr(name, '.') == NULL) {
        
    } else if (strlen(name) - (strchr(name, '.') - name) > MAX_EXTENSION + 1) {
        printf("invalid name:  MAX_extension(3) reached\n");
        return -1;
    }
    
    int inode_idx = get_inode_from_name(name);
    
    // Check if file is already open
    int i;
    for(i = 1; i < MAX_FILES; i++) {
        if( (fd_table[i].status == 0) && (fd_table[i].inode_idx == inode_idx)) return i;
    }
    
    int fd = find_a_free_fd();
    if(fd == -1) {
        printf("no available file descriptor being found\n");
        return -1;
    }
    
    //create file here
    if(inode_idx == -1)
    {
        inode_idx = find_a_free_inode();
        inode = &inode_table[inode_idx];
        printf("creating the file named: %s (%d)\n",name,inode_idx);
           
        new_file.status = 0;
        new_file.inode_idx = inode_idx;
        strcpy(new_file.name, name);
        
        int root_dir_idx = find_a_root_slot();
        root_dir[root_dir_idx] = new_file;
        
        write_blocks(ROOT_DIR_BASE, dir_blocks, root_dir);
        
        fd_table[ROOT_FD].rw_ptr = inode_table[fd_table[ROOT_FD].inode_idx].size;
        sfs_fwrite(ROOT_FD, (char*)&new_file, sizeof(dir_entry_t));
    } else {
        //file already exists
        inode = &inode_table[inode_idx];
    }
    
    fd_table[fd].inode_idx = inode_idx;
    fd_table[fd].rw_ptr = inode->size;

    
    return fd;
}



int sfs_fclose(int fileID){
    
    if(fd_table[fileID].status == 1){
        printf("the file IDed: %d is not opened for closing\n", fileID);
        return -1;
    }

    fd_table[fileID].inode_idx = -1;
    fd_table[fileID].status = 1;
    return 0;
}

unsigned int max(unsigned int A, unsigned int B){
    if(A<=B){
        return B;
    }
    return A;
}

int sfs_fread(int fileID, char *buf, int length){
    int block_ptr, last_full_block, last_block_offset;
    char block[MAX_BLOCKS];
    
    if(fd_table[fileID].status == 1){
        printf("the file IDed: %d is not opened for reading\n", fileID);
        return -1;
    }
    
    inode_t* inode = &inode_table[fd_table[fileID].inode_idx];

    if(fd_table[fileID].rw_ptr + length > inode->size) {
        // printf("The end of read ( %d ) will exceed the file(id: %d)'s end (%d). Read until the end\n",fd_table[fileID].rw_ptr + length , fileID,inode->size);
        length = inode->size - fd_table[fileID].rw_ptr;
    }
    
    int rw_ptr = fd_table[fileID].rw_ptr;
    int block_num = rw_ptr/BLOCK_SIZE;
    int block_offset = rw_ptr % BLOCK_SIZE;

    if( (block_offset + length) <= BLOCK_SIZE) {
        block_ptr = get_block_ptr(&(inode->block_ptr), block_num);
        read_blocks(block_ptr, 1, block);
        memcpy(buf, &block[block_offset], length);
        fd_table[fileID].rw_ptr += length;
        return length;
    }else{
    
        last_full_block = ((block_offset + length) / BLOCK_SIZE) + block_num;
        
        block_ptr = get_block_ptr(&(inode->block_ptr), block_num);
        read_blocks(block_ptr, 1, block);
        memcpy(buf, &block[block_offset], BLOCK_SIZE - block_offset);
        buf += BLOCK_SIZE - block_offset;
        block_num++;    

        int remaining_length = length - (BLOCK_SIZE - block_offset);
        while(remaining_length - BLOCK_SIZE > 0) {
            block_ptr = get_block_ptr(&(inode->block_ptr), block_num);
            read_blocks(block_ptr, 1, block);
            memcpy(buf, block, BLOCK_SIZE);
            remaining_length -= BLOCK_SIZE;
            buf += BLOCK_SIZE;
            block_num++;

        }
        

        block_ptr = get_block_ptr(&(inode->block_ptr), block_num);
        read_blocks(block_ptr, 1, block);
        memcpy(buf, block, remaining_length);
        
        fd_table[fileID].rw_ptr += length;
        
        return length;
    }
  //     int rw_ptr = fd_table[fileID].rw_ptr;
  // int block_num = rw_ptr/BLOCK_SIZE;
  // int block_offset = rw_ptr % BLOCK_SIZE;

  // int block_to_read = get_block_ptr(&(inode->block_ptr), block_num);
  // read_blocks(block_ptr, 1, block);
  // memcpy(buf, &block[block_offset], BLOCK_SIZE - block_offset);
  // buf += BLOCK_SIZE - block_offset;
  // block_num++;    

  // int remaining_length = length - (BLOCK_SIZE - block_offset);
  // while(remaining_length>0){
  //    block_ptr = get_block_ptr(&(inode->block_ptr), block_num);
  //    read_blocks(block_ptr, 1, block);
  //    memcpy(buf, block, BLOCK_SIZE);
  //    buf += BLOCK_SIZE;
  //    remaining_length -=  BLOCK_SIZE;
  //    block_num++;

  // }

  // fd_table[fileID].rw_ptr += length;

  // memset(fd_table[fileID].rw_ptr,0,BLOCK_SIZE-(fd_table[fileID].rw_ptr%BLOCK_SIZE));
    
  //   return length;
}

int sfs_fseek(int fileID, int offset){

    if(fd_table[fileID].status == 1){
        printf("the file IDed: %d is not opened for seeking\n", fileID);
        return -1;
    }
    // printf("here");
    if(offset > inode_table[fd_table[fileID].inode_idx].size){
        // printf("The offset is beyond file size, reset to appending mode\n");
        offset = inode_table[fd_table[fileID].inode_idx].size;
        return -1;
    }
    
    fd_table[fileID].rw_ptr = offset;    
    return 0;
}

int ceil_div(int A, int B){
    if (A%B==0) return A/B;
    return A/B+1;
}

int sfs_fwrite(int fileID, const char *buf, int length){
    char block[MAX_BLOCKS];
    
    if( fd_table[fileID].status == 1 ){
        printf("the file IDed: %d is not opened for writing\n", fileID);
        return -1;
    }
    if(fd_table[fileID].rw_ptr + length > MAX_FILE_SIZE){
        printf("The new file sized will exceed the MAX_FILE_SIZE. Declined the request\n");
        return -1;
    }
    
    inode_t* inode = &inode_table[fd_table[fileID].inode_idx];
    int rw_ptr = fd_table[fileID].rw_ptr;
    
    int block_num = rw_ptr/BLOCK_SIZE;
    int block_offset = rw_ptr % BLOCK_SIZE;
    
    // no ovesize a block
    if((block_offset + length) <= BLOCK_SIZE) {
       int block_ptr = get_block_ptr(&(inode->block_ptr), block_num);
        //no free blocks
        if(block_ptr == -1){

            return -1;
        }
        read_blocks(block_ptr, 1, block);
        memcpy(&block[block_offset], buf, length);
        write_blocks(block_ptr, 1, block);
        //update size of file

        int new_len = max(rw_ptr + length, inode->size);
        sfs_fseek(fileID,rw_ptr + length);
        inode->size = new_len;
        
        write_blocks(INODE_TABLE_BASE, num_blocks, inode_table);

        return length;

    }else{
        //need new blocks
        int available_block=0;
        int i;
        for(i = BLOCK_BIT_MAP_BASE + 1; i < MAX_BLOCKS; i++) {
            if(free_blocks[i] == 1) {
                available_block++;
            }
        }
        if(available_block < ceil_div(rw_ptr+length,BLOCK_SIZE) - ceil_div(inode_table[fileID].size,BLOCK_SIZE)){
            printf("appending the length of %d will exceed the total block size. reject the quest\n", length);
            return -1;
        }


        int length_counter = 0;
        int last_full_block = ((block_offset + length) / BLOCK_SIZE) + block_num;
        
        int block_ptr = get_block_ptr(&(inode->block_ptr), block_num);
        if(block_ptr == -1){
            printf("desgin prob? unable to opend the last block of a file\n");
            return -1;
        }

        if(block_offset!=0){
            read_blocks(block_ptr, 1, block);
            memcpy(&block[block_offset], buf, BLOCK_SIZE - block_offset);
            length_counter += BLOCK_SIZE - block_offset;
            write_blocks(block_ptr, 1, block);   
            
            buf += BLOCK_SIZE - block_offset;
            block_num++;
        }
        

        while(length_counter + BLOCK_SIZE< length ) {
            block_ptr = get_block_ptr(&(inode->block_ptr), block_num);
            if(block_ptr == -1){
                printf("no available block to write\n");
                return -1;
             }
            memcpy(block, buf, BLOCK_SIZE);
            length_counter += BLOCK_SIZE;
            write_blocks(block_ptr, 1, block);
            
            buf += BLOCK_SIZE;
            block_num++;
        }
        if(length_counter != length){
        block_ptr = get_block_ptr(&(inode->block_ptr), block_num);
            if(block_ptr == -1){
                printf("no available block to write\n");
                return -1;
            }
            read_blocks(block_ptr, 1, block);
            memcpy(block, buf, (block_offset + length) % BLOCK_SIZE);
            length_counter += (block_offset + length) % BLOCK_SIZE;
            write_blocks(block_ptr, 1, block);
        }
        int new_len = max(rw_ptr + length, inode->size);
        inode->size = new_len;
        sfs_fseek(fileID,rw_ptr + length);
    
        write_blocks(INODE_TABLE_BASE, num_blocks, inode_table);


        
        return length_counter;
    }   
    // char block[MAX_BLOCKS];
    // inode_t* inode = inode_table[fd_table[fileID].inode_idx];
    // int tmp = fd_table[fileID].rw_ptr ;
    // char* address = block;
    // // memset(&address,0,MAX_FILE_SIZE);
    // fd_table[fileID].rw_ptr = 0;
    // sfs_fread(fileID, &block[0], inode->size);
    // // free_block_ptrs(&(inode->block_ptr));
    // memcpy(&address, buf, length);
    // int remaining_length = tmp +  length;
    // int i=0;
    // while(remaining_length >0 ){
    //     int block = get_block_ptr(&(inode->block_ptr),i);
    //     if(block==-1){
    //         return -1;
    //     }

    //     write_blocks(block, &address,BLOCK_SIZE);
    //     address += BLOCK_SIZE;
    //     remaining_length -= BLOCK_SIZE;
    //     i++;


    // }
}

void remove_file_from_directory(char* name)
{
    

    int i;
    for(i = 0; i < MAX_INODES - 1 ; i++) {
        if(strcmp(root_dir[i].name, name) == 0) {
            
            memmove(&root_dir[i], &root_dir[i+1], sizeof(dir_entry_t)*(MAX_INODES - i - 2));
            
            
            dir_entry_t temp;
            temp.status = 1;
            memcpy(&root_dir[MAX_INODES - 2], &temp, sizeof(dir_entry_t));
            
            
            write_blocks(ROOT_DIR_BASE, dir_blocks, root_dir);
            
 
            return;
        }
    }
}

int sfs_remove(char *file) {
    
    int inode_num;
    
    inode_num = get_inode_from_name(file);
    if(inode_num == -1) {
        printf("no file named: %s existing file system to remove\n",file);
        return -1;
    }

    free_block_ptrs(&(inode_table[inode_num].block_ptr));

    remove_file_from_directory(file);

    int dir_size = inode_table[fd_table[ROOT_FD].inode_idx].size;

    fd_table[ROOT_FD].rw_ptr = 0;
    inode_table[fd_table[ROOT_FD].inode_idx].size = 0;

    free_block_ptrs(&inode_table[fd_table[ROOT_FD].inode_idx].block_ptr);
    memset(&inode_table[fd_table[ROOT_FD].inode_idx], 0, sizeof(inode_t));
    sfs_fwrite(ROOT_FD, (char*)root_dir, dir_size - sizeof(dir_entry_t));
    

    write_blocks(INODE_TABLE_BASE, num_blocks, inode_table);
    free_inodes[inode_num] = 1;
    write_blocks(INONDE_MAP_BASE, 1, free_inodes);

    memset(&inode_table[inode_num], 0, sizeof(inode_t));
    write_blocks(INODE_TABLE_BASE, num_blocks, inode_table);
    return 0;
}


int find_a_root_slot() {
    int i;
    
    for(i = 0; i < MAX_INODES - 1; i++) {
        if(root_dir[i].status == 1) {
            return i;
        }
    }
    printf("root directory is full\n");
    return -1;
}

int find_a_free_fd()
{
    int i;
    
    for(i = 1; i < MAX_FILES; i++) {
        if(fd_table[i].status == 1) {
            fd_table[i].status = 0;
            return i;
        }
    }
    
    return -1;
}



int get_inode_from_name(const char* name) {
    int i, inode_idx;
    
    for(i = 0; i < MAX_INODES - 1; i ++) {
        if(strcmp(root_dir[i].name, name) == 0) {
            inode_idx = root_dir[i].inode_idx;
            return inode_idx;
        }
    }
    return -1;
}


int find_a_free_inode()
{
    int i;
    
    for(i = 1; i < MAX_INODES; i++) {
        if(free_inodes[i] == 1) {
            free_inodes[i] = 0;
            write_blocks(INONDE_MAP_BASE, 1, free_inodes);
            return i;
        }
    }
    
    return -1;
}

int find_a_free_datablock()
{
    int i;
    for(i = 0; i < MAX_BLOCKS; i++) {
        if(free_blocks[i] == 1) {
            free_blocks[i] = 0;
            write_blocks(BLOCK_BIT_MAP_BASE, 1, free_blocks);
            return BLOCK_BIT_MAP_BASE + 1 + i;
        }
    }
    printf("Ran out of free data blocks\n");
    return 0;
}


int get_block_ptr(block_ptr_t* pointers, int block_num)
{   

    if(block_num < SINGLE_PTR_NUM) {
        if(pointers->direct[block_num] == 0) {
             int new_block = find_a_free_datablock();
            if( new_block== 0){
                return -1;
            }
            pointers->direct[block_num] = new_block;
            return new_block;
        }else{
            return pointers->direct[block_num];//keep the original one if exists
        }
    }
    
    int indirect_pointers[BLOCK_SIZE/PTR_SIZE];
    if(pointers->indirect == 0) {//initialize the single - indirect if necessary
        if( (pointers->indirect = find_a_free_datablock()) == 0){
            return -1;
        }
    
        memset(indirect_pointers, 0, BLOCK_SIZE);//establish new block for pointers
        write_blocks(pointers->indirect, 1, (void*)indirect_pointers);
    }
    
    read_blocks(pointers->indirect, 1, (void*)indirect_pointers);

    if(indirect_pointers[block_num - SINGLE_PTR_NUM] == 0) {
        if( (indirect_pointers[block_num - SINGLE_PTR_NUM] = find_a_free_datablock()) == 0)
            return -1;
        write_blocks(pointers->indirect, 1, (void*)indirect_pointers);
    }

    return indirect_pointers[block_num - SINGLE_PTR_NUM];
}

void free_block_ptrs(block_ptr_t* pointers)
{
    int indirect_pointers[BLOCK_SIZE/PTR_SIZE];
    int index, block_num;
    
    for(block_num = 0; block_num < SINGLE_PTR_NUM; block_num++) {
        index = pointers->direct[block_num];
        if(index == 0) {
            write_blocks(BLOCK_BIT_MAP_BASE, 1, free_blocks);
            return;
        }
        
        free_blocks[index] = 1;
    }

    memset(indirect_pointers, 0, BLOCK_SIZE);
    read_blocks(pointers->indirect, 1, (void*)indirect_pointers);

    for(block_num = 0; block_num < BLOCK_SIZE/PTR_SIZE; block_num++) {
        index = indirect_pointers[block_num];
        if(index == 0) {
            write_blocks(BLOCK_BIT_MAP_BASE, 1, free_blocks);
            return;
        }
        indirect_pointers[block_num] = 0;
        free_blocks[index] = 1;
    }
}



void init_super(){
    super.magic = MAGIC_NUM;
    super.block_size = BLOCK_SIZE;
    super.fs_size = MAX_BLOCKS*BLOCK_SIZE;
    super.inode_table_len = MAX_INODES;
    super.root_dir_inode = ROOT_INDEX;
}

void free_root() {
    int i;
    for(i = 0; i < MAX_INODES - 1 ; i++) {
        root_dir[i].status = 1;
    }
}

void init_root(){
    free_inodes[ROOT_INDEX] = 0;

    inode_table[ROOT_INDEX].mode =  0x755;
    inode_table[ROOT_INDEX].link_cnt = 0;
    inode_table[ROOT_INDEX].uid = 0;
    inode_table[ROOT_INDEX].gid = 0;
    inode_table[ROOT_INDEX].size = 0;

    free_root();
}




int mksfs(int fresh) {
    
    int i;

    if (fresh) {
        init_fresh_disk(DISK_FILE, BLOCK_SIZE, MAX_BLOCKS);

        // super = (void*)calloc(1,sizeof(super_block_t));
        // inode_table = (void*)calloc(MAX_INODES,sizeof(inode_t));
        // root_dir = (void*)calloc(MAX_FILES,sizeof(dir_entry_t));
        // free_blocks = (void*)calloc(1, BLOCK_SIZE);
        // free_inodes = (void*)calloc(1,BLOCK_SIZE);
        // fd_table = (void*)calloc((MAX_FILES,sizeof(fd_table_t));

        bzero(&super, sizeof(super_block_t));
        bzero(&inode_table[0], sizeof(inode_t)*MAX_INODES);
        bzero(&root_dir[0], sizeof(dir_entry_t)*(MAX_FILES));
        bzero(&free_blocks[0], BLOCK_SIZE);
        bzero(&free_inodes[0], BLOCK_SIZE);
        bzero(&fd_table[0], sizeof(fd_table_t)*MAX_FILES);

        memset(free_inodes, 1, BLOCK_SIZE);
        memset(free_blocks, 1, BLOCK_SIZE);
        
        for(i = 0; i < MAX_FILES; i++){
            fd_table[i].status = 1;
        }
      
        init_super();
        init_root();

        fd_table[ROOT_FD].status = 0;
        fd_table[ROOT_FD].inode_idx = ROOT_INDEX;
        fd_table[ROOT_FD].rw_ptr = 0;
        
        write_blocks(SUPER_BLOCK_BASE, 1, &super);
        write_blocks(INODE_TABLE_BASE, num_blocks, inode_table);
        write_blocks(ROOT_DIR_BASE, dir_blocks, root_dir);
        write_blocks(INONDE_MAP_BASE, 1, inode_table);
        write_blocks(BLOCK_BIT_MAP_BASE, 1, free_blocks);
        
        return 0;

    } else {
        
        for(i = 0; i < MAX_FILES; i++){
            fd_table[i].status = 1;
        }
        
        init_disk(DISK_FILE, BLOCK_SIZE, MAX_BLOCKS);
         // write_blocks(SUPER_BLOCK_BASE, 1, &super);
        read_blocks(INONDE_MAP_BASE, 1, (void*) free_inodes);
        read_blocks(BLOCK_BIT_MAP_BASE, 1, (void*) free_blocks);
        read_blocks(ROOT_DIR_BASE, dir_blocks, (void*) root_dir);
        read_blocks(INODE_TABLE_BASE, num_blocks, (void*) inode_table);

        fd_table[ROOT_FD].status = 0;
        fd_table[ROOT_FD].inode_idx = ROOT_INDEX;
        fd_table[ROOT_FD].rw_ptr = inode_table[fd_table[ROOT_FD].inode_idx].size;

        return 0;
    }
}
