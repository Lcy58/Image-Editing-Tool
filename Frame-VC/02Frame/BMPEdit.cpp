// BMPEdit.cpp : implementation file
//

#include "stdafx.h"
#include "02test.h"
#include "BMPEdit.h"
#include "math.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

/////////////////////////////////////////////////////////////////////////////
// CBMPEdit

CBMPEdit::CBMPEdit()
{
}

CBMPEdit::~CBMPEdit()
{
}



/////////////////////////////////////////////////////////////////////////////
// CBMPEdit message handlers


BOOL CBMPEdit::LoadBmpFile(const CString &sBmpFileName)
{
	m_bReady = FALSE;

	//�ͷ�֮ǰ��ȡ������///////
	//�ͷ�ͼ������
	if(m_hgImageData)
		GlobalFree(m_hgImageData);
	
	m_sBMPFileFullName = sBmpFileName;
	if(m_sBMPFileFullName.IsEmpty())
		return FALSE;
	CFile BmpFile;
	if(!BmpFile.Open(m_sBMPFileFullName,CFile::modeRead))
	{
		AfxMessageBox("�ļ���ʱ����!");
		return FALSE;
	}
	//��ʽ���
	if(BmpFile.Read(&m_BmpFileHeader,sizeof(BITMAPFILEHEADER)) < sizeof(BITMAPFILEHEADER))
	{
		AfxMessageBox("�ļ���������!");
		BmpFile.Close();
		return FALSE;
	}
	if(m_BmpFileHeader.bfType != 0x4D42)
	{
		AfxMessageBox("��λͼ�ļ�");
		BmpFile.Close();
		return FALSE;
	}
	if(BmpFile.Read(&m_BmpInfo.bmiHeader,sizeof(BITMAPINFOHEADER)) < sizeof(BITMAPINFOHEADER))
	{
		AfxMessageBox("�ļ���������!");
		BmpFile.Close();
		return FALSE;
	}
	//SetDIBitsToDevice()��StretchDIBits()�����д����ɫ�壬����Ҫ����ǰDC��ѡ���ɫ���ˣ�����
	
	//��ȡ��ɫ��
	if(m_BmpInfo.bmiHeader.biBitCount < 16)
		BmpFile.Read(&m_BmpInfo.bmiColors,GetColorNum(m_BmpInfo.bmiHeader) * sizeof(RGBQUAD));
	
	//��ȡͼ������
	long nBytePerLine = (m_BmpInfo.bmiHeader.biWidth*m_BmpInfo.bmiHeader.biBitCount + 31)/32*4;//ͼ��ÿ����ռʵ���ֽ���������4�ı�����
	m_hgImageData = GlobalAlloc(GHND, nBytePerLine * m_BmpInfo.bmiHeader.biHeight);

	if(m_hgImageData == NULL)
	{
		AfxMessageBox("�ڴ治��");
		BmpFile.Close();
		return FALSE;
	}
	byte *pImageData = (byte *)GlobalLock(m_hgImageData);
	BmpFile.Read(pImageData,nBytePerLine * m_BmpInfo.bmiHeader.biHeight);
    
	//add your code here








	GlobalUnlock(m_hgImageData);
	
	BmpFile.Close();
	m_bReady = TRUE;
	

	return TRUE;
}

int CBMPEdit::GetColorNum(const BITMAPINFOHEADER &BmpInf)
{
	if(BmpInf.biClrUsed >0)
		return BmpInf.biClrUsed;
	else
		return 1 << BmpInf.biBitCount;
}

BOOL CBMPEdit::PlayBmp(CDC *pDC, const CPoint &StartPoint)
{	
	if(!m_bReady)
		return FALSE;
	byte *pData = (byte *)GlobalLock(m_hgImageData);

	if(!SetDIBitsToDevice(pDC->GetSafeHdc(),
						StartPoint.x,StartPoint.y,
						m_BmpInfo.bmiHeader.biWidth,m_BmpInfo.bmiHeader.biHeight,
						0,0,
						0,m_BmpInfo.bmiHeader.biHeight,//
						pData,
					//	pBmpInf,
					    (BITMAPINFO*)&m_BmpInfo,
						DIB_RGB_COLORS
						))
	 {
		 GlobalUnlock(m_hgImageData);
		 return FALSE;
	 }

	GlobalUnlock(m_hgImageData);
	return TRUE;

}
