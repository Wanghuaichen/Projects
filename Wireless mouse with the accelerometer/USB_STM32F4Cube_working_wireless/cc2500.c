/**
  ******************************************************************************
  * @file    cc2500.c
  * @version V1.0.0
  * @date    12-01-2018
  * @brief   cc2500 wireless driver
  ****************************************************************************** 
*/


#include <cc2500.h>
#include <stm32f4xx.h>
#include <cmsis_os.h>

 #include "stm32f4xx_hal.h"
 #include "stm32f4xx_hal_gpio.h"
 #include "stm32f4xx_hal_rcc.h"
 #include "stm32f4xx_hal_spi.h"

__IO uint32_t  CC2500Timeout = CC2500_FLAG_TIMEOUT; 

SPI_HandleTypeDef SPI_Handle;
uint8_t command_strobe_response,num_bytes_in_FIFO;

/* Read/Write command */
#define READWRITE_CMD              ((uint8_t)0x80) 
/* Multiple byte read/write command */ 
#define MULTIPLEBYTE_CMD           ((uint8_t)0x40)
/* Dummy Byte Send by the SPI Master device in order to generate the Clock to the Slave device */
#define DUMMY_BYTE                 ((uint8_t)0x00)

/**
  * @brief  Writes a block of data to the cc2500.
  * @param  pBuffer : pointer to the buffer  containing the data to be written to the cc2500.
  * @param  WriteAddr : cc2500's internal address to write to.
  * @param  NumByteToWrite: Number of bytes to write.
  * @retval None
  */
void cc2500_Write(uint8_t* pBuffer, uint8_t WriteAddr, uint16_t NumByteToWrite)
{
  /* Configure the MS bit: 
       - When 0, the address will remain unchanged in multiple read/write commands.
       - When 1, the address will be auto incremented in multiple read/write commands.
  */
  if(NumByteToWrite > 0x01)
  {
    WriteAddr |= (uint8_t)MULTIPLEBYTE_CMD;
  }
  /* Set chip select Low at the start of the transmission */
  CC2500_NSS_LOW();
  
  /* Send the Address of the indexed register */
  cc2500_SendByte(WriteAddr);
  /* Send the data that will be written into the device (MSB First) */
  while(NumByteToWrite >= 0x01)
  {
    cc2500_SendByte(*pBuffer);
    NumByteToWrite--;
    pBuffer++;
  }
  
  /* Set chip select High at the end of the transmission */ 
  CC2500_NSS_HIGH();
}

/**
  * @brief  Reads a block of data from the cc2500.
  * @param  pBuffer : pointer to the buffer that receives the data read from the cc2500.
  * @param  ReadAddr : cc2500's internal address to read from.
  * @param  NumByteToRead : number of bytes to read from the cc2500.
  * @retval None
  */
void cc2500_Read(uint8_t* pBuffer, uint8_t ReadAddr, uint16_t NumByteToRead)
{  
  if(NumByteToRead > 0x01)
  {
    ReadAddr |= (uint8_t)(READWRITE_CMD | MULTIPLEBYTE_CMD);
  }
  else
  {
    ReadAddr |= (uint8_t)READWRITE_CMD;
  }
  /* Set chip select Low at the start of the transmission */
  CC2500_NSS_LOW();
  
  /* Send the Address of the indexed register */
  cc2500_SendByte(ReadAddr);
  
  /* Receive the data that will be read from the device (MSB First) */
  while(NumByteToRead > 0x00)
  {
    /* Send dummy byte (0x00) to generate the SPI clock to cc2500 (Slave device) */
    *pBuffer = cc2500_SendByte(DUMMY_BYTE);
    NumByteToRead--;
    pBuffer++;
  }
  
  /* Set chip select High at the end of the transmission */ 
  CC2500_NSS_HIGH();
}



/**
  * @brief  Initializes the low level interface used to drive the cc2500
  * @param  None
  * @retval None
  */

void cc2500_gpio_init() {
	GPIO_InitTypeDef GPIO_InitStructure;

  /* Enable the SPI periph */
  __SPI2_CLK_ENABLE();

  /* Enable CS, INT1, INT2  GPIO clock */
  __GPIOB_CLK_ENABLE();

  GPIO_InitStructure.Mode  = GPIO_MODE_AF_PP;
  GPIO_InitStructure.Pull  = GPIO_PULLDOWN;
  GPIO_InitStructure.Speed = GPIO_SPEED_MEDIUM;
  GPIO_InitStructure.Alternate = GPIO_AF5_SPI2;

  GPIO_InitStructure.Pin = CC2500_SPI_SCK_PIN;
  HAL_GPIO_Init(CC2500_SPI_SCK_GPIO_PORT, &GPIO_InitStructure);

  /* SPI  MOSI pin configuration */
  GPIO_InitStructure.Pin =  CC2500_SPI_MOSI_PIN;
  HAL_GPIO_Init(CC2500_SPI_MOSI_GPIO_PORT, &GPIO_InitStructure);

  /* SPI MISO pin configuration */
  GPIO_InitStructure.Pin = CC2500_SPI_MISO_PIN;
  HAL_GPIO_Init(CC2500_SPI_MISO_GPIO_PORT, &GPIO_InitStructure);
	
	GPIO_InitStructure.Pin   = CC2500_SPI_NSS_PIN;
  GPIO_InitStructure.Mode  = GPIO_MODE_OUTPUT_PP;
  GPIO_InitStructure.Speed = GPIO_SPEED_FREQ_MEDIUM;
  HAL_GPIO_Init(CC2500_SPI_NSS_GPIO_PORT, &GPIO_InitStructure);

  /* Deselect : Chip Select high */
  HAL_GPIO_WritePin(CC2500_SPI_NSS_GPIO_PORT, CC2500_SPI_NSS_PIN, GPIO_PIN_SET);

}

void cc2500_LowLevel_Init(void)
{
	command_strobe_response = 0;
	num_bytes_in_FIFO = 0;
    
	GPIO_InitTypeDef GPIO_InitStructure;

	__HAL_RCC_SPI2_CLK_ENABLE();

  /* SPI configuration -------------------------------------------------------*/
	HAL_SPI_DeInit(&SPI_Handle);
	SPI_Handle.Instance = SPI2;
  SPI_Handle.Init.Direction = SPI_DIRECTION_2LINES;
  SPI_Handle.Init.DataSize = SPI_DATASIZE_8BIT;
  SPI_Handle.Init.CLKPolarity = SPI_POLARITY_LOW;
  SPI_Handle.Init.CLKPhase = SPI_PHASE_1EDGE;
	SPI_Handle.Init.CRCCalculation	= SPI_CRCCALCULATION_DISABLED;
  SPI_Handle.Init.NSS = SPI_NSS_SOFT;
  SPI_Handle.Init.BaudRatePrescaler = SPI_BAUDRATEPRESCALER_32;	// Baudrate set to 8, gives a frequency of 5.25 MHz by dividing bus fequency of 42MHz by 8. The maximum possible frequency is 6.5 MHz for burst access as specified in the data sheet of the ez430 CC2500 chipset
  SPI_Handle.Init.FirstBit = SPI_FIRSTBIT_MSB;
	SPI_Handle.Init.CRCPolynomial = 7;
	SPI_Handle.Init.TIMode = SPI_TIMODE_DISABLED;
  SPI_Handle.Init.Mode = SPI_MODE_MASTER;
	
	if (HAL_SPI_Init(&SPI_Handle) != HAL_OK) {printf ("ERROR: Error in initialising SPI1 \n");} else {printf("ok\n");};
	
	cc2500_gpio_init();
	
	__HAL_SPI_ENABLE(&SPI_Handle);
	
	uint8_t command = 0;
	cc2500_Write(&command, 0x30, 1);
	
	cc2500_configure_registers();
}

/**
	* @brief Configures the CC2500 register values to required configuration
	* @param  None
  * @retval None
  */
void cc2500_configure_registers(){
		//delay before setting register values
		osDelay(100);
		uint8_t configuration_values[36]={VAL_CC2500_IOCFG2,	VAL_CC2500_IOCFG0,	VAL_CC2500_FIFOTHR,	VAL_CC2500_PKTLEN,	VAL_CC2500_PKTCTRL1,
						VAL_CC2500_PKTCTRL0, VAL_CC2500_ADDR, VAL_CC2500_CHANNR,	VAL_CC2500_FSCTRL1,	VAL_CC2500_FSCTRL0,	
						VAL_CC2500_FREQ2,	VAL_CC2500_FREQ1,	VAL_CC2500_FREQ0,	VAL_CC2500_MDMCFG4,	VAL_CC2500_MDMCFG3,
						VAL_CC2500_MDMCFG2,	VAL_CC2500_MDMCFG1,	VAL_CC2500_MDMCFG0,	VAL_CC2500_DEVIATN,	VAL_CC2500_MCSM1, VAL_CC2500_MCSM0,
						VAL_CC2500_FOCCFG,	VAL_CC2500_BSCFG,	VAL_CC2500_AGCTRL2,	VAL_CC2500_AGCTRL1,	VAL_CC2500_AGCTRL0,	VAL_CC2500_FREND1,
						VAL_CC2500_FREND0,	VAL_CC2500_FSCAL3,	VAL_CC2500_FSCAL2,	VAL_CC2500_FSCAL1,	VAL_CC2500_FSCAL0, VAL_CC2500_FSTEST,
						VAL_CC2500_TEST2,	VAL_CC2500_TEST1,	VAL_CC2500_TEST0};
	
	uint8_t configuration_adresses[36]= {CC2500_IOCFG2,	CC2500_IOCFG0,	CC2500_FIFOTHR,	CC2500_PKTLEN,	CC2500_PKTCTRL1,	
						CC2500_PKTCTRL0,CC2500_ADDR,CC2500_CHANNR,CC2500_FSCTRL1,CC2500_FSCTRL0,CC2500_FREQ2,	
						CC2500_FREQ1,	CC2500_FREQ0,	CC2500_MDMCFG4,	CC2500_MDMCFG3,	CC2500_MDMCFG2,	
						CC2500_MDMCFG1,	CC2500_MDMCFG0,	CC2500_DEVIATN,	CC2500_MCSM1,	CC2500_MCSM0,
						CC2500_FOCCFG,	CC2500_BSCFG,	CC2500_AGCTRL2,	CC2500_AGCTRL1,	CC2500_AGCTRL0,
						CC2500_FREND1,	CC2500_FREND0,	CC2500_FSCAL3,	CC2500_FSCAL2,	CC2500_FSCAL1,
						CC2500_FSCAL0,	CC2500_FSTEST,	CC2500_TEST2,	CC2500_TEST1,	CC2500_TEST0};
						
	for(int i=0; i<36; i++){
		cc2500_Write(configuration_values+i,configuration_adresses[i],1);
	}				
}


/**
  * @brief  Sends a Byte through the SPI interface and return the Byte received 
  *         from the SPI bus.
  * @param  Byte : Byte send.
  * @retval The received byte value
  */
uint8_t cc2500_SendByte(uint8_t byte)
{
	CC2500Timeout = CC2500_FLAG_TIMEOUT;
  while (__HAL_SPI_GET_FLAG(&SPI_Handle, SPI_FLAG_TXE) == RESET)
  {
    if((CC2500Timeout--) == 0) return -1;
  }
	
  /* Send a Byte through the SPI peripheral */
  cc2500_SPI_SendData(&SPI_Handle,  byte);

  /* Wait to receive a Byte */
  CC2500Timeout = CC2500_FLAG_TIMEOUT;
  while (__HAL_SPI_GET_FLAG(&SPI_Handle, SPI_FLAG_RXNE) == RESET)
  {
    if((CC2500Timeout--) == 0) {
			return -1;
		}
  }

  /* Return the Byte read from the SPI bus */ 
  return cc2500_SPI_ReceiveData(&SPI_Handle);
}

uint8_t cc2500_SPI_ReceiveData(SPI_HandleTypeDef *hspi)
{ 
  /* Write in the DR register the data to be sent */
	return hspi->Instance->DR;
}

void cc2500_SPI_SendData(SPI_HandleTypeDef *hspi, uint8_t byte)
{ 
  /* Write in the DR register the data to be sent */
	hspi->Instance->DR = byte;
}

uint8_t cc2500_Receive_Data(uint8_t* output_array){
	//code for overflow handling
	cc2500_Send_Command_Strobe(&command_strobe_response, CC2500_SRX);
	
	//shift the strobe response to ignore the last 4 bits
	if(command_strobe_response>>4 == RX_OVERFLOW){
		printf("overflow\n");
		cc2500_Send_Command_Strobe(&command_strobe_response, CC2500_SFRX);
		num_bytes_in_FIFO = 0;
	}
	
	//read the RX FIFO and write to output_array
	else{	
		cc2500_Send_Command_Strobe(&command_strobe_response, CC2500_SRX);
		//find out how much data is in RX FIFO
		cc2500_Read_Status_Register(&num_bytes_in_FIFO, CC2500_RXBYTES);
		printf("RXBYTES %d\n", num_bytes_in_FIFO);
		cc2500_Read(output_array, CC2500_FIFO,num_bytes_in_FIFO);
	}
	return num_bytes_in_FIFO;
}

void cc2500_Send_Command_Strobe(uint8_t* pBuffer, uint8_t ReadAddr)
{ 
	ReadAddr |= (uint8_t)(READWRITE_CMD);
	cc2500_One_Byte_Read(pBuffer, ReadAddr);
}


/**
  * @brief  Reads a status register (it sends a burst read command).
	* @param  pBuffer: where the read data is written
	* @param  ReadAddr: the read address.
  * @retval None
  */
void cc2500_Read_Status_Register(uint8_t* pBuffer, uint8_t ReadAddr)
{ 
	ReadAddr |= (uint8_t)(READWRITE_CMD | MULTIPLEBYTE_CMD);
	cc2500_One_Byte_Read(pBuffer, ReadAddr);
}


/**
  * @brief  Reads one byte from address. Used for sending command strobes and reading status registers.
	* @param  pBuffer: where the read data is written
	* @param  ReadAddr: the read address.
  * @retval None
  */
void cc2500_One_Byte_Read(uint8_t* pBuffer, uint8_t ReadAddr)
{
	 /* Set chip select Low at the start of the transmission */
  CC2500_NSS_LOW();
  
  /* Send the Address of the indexed register */
  cc2500_SendByte(ReadAddr);
  
	/* Send dummy byte (0x00) to generate the SPI clock to cc2500 (Slave device) */
	*pBuffer = cc2500_SendByte(DUMMY_BYTE);
	pBuffer++;
	
  /* Set chip select High at the end of the transmission */ 
  CC2500_NSS_HIGH();
}


