/**
 * SudhaniHub Payment Service Abstraction
 * Supports Cash On Delivery, UPI Deep Linking, and Razorpay Integration
 */

export interface PaymentOrderPayload {
  orderId: string;
  amountInRupees: number;
  customerName: string;
  customerPhone: string;
  customerEmail: string;
}

export interface PaymentResult {
  success: boolean;
  transactionId: string;
  gateway: 'RAZORPAY' | 'COD' | 'UPI_DIRECT';
  status: 'PENDING' | 'SUCCESS' | 'FAILED';
  message: string;
}

export class PaymentService {
  private razorpayKeyId: string;
  private razorpaySecret: string;

  constructor() {
    this.razorpayKeyId = process.env.RAZORPAY_KEY_ID || 'rzp_test_mock';
    this.razorpaySecret = process.env.RAZORPAY_KEY_SECRET || 'rzp_secret_mock';
  }

  /**
   * Create an online checkout transaction for Razorpay / UPI
   */
  async createOnlineOrder(payload: PaymentOrderPayload): Promise<any> {
    // Standard Razorpay Order Creation format (amount in paise)
    const amountInPaise = Math.round(payload.amountInRupees * 100);

    return {
      razorpayOrderId: `rzp_order_${payload.orderId}_${Date.now()}`,
      amount: amountInPaise,
      currency: 'INR',
      keyId: this.razorpayKeyId,
      customer: {
        name: payload.customerName,
        contact: payload.customerPhone,
        email: payload.customerEmail
      },
      upiIntentUri: `upi://pay?pa=sudhanihub@icici&pn=SudhaniHub&am=${payload.amountInRupees}&tr=${payload.orderId}&cu=INR`
    };
  }

  /**
   * Verify Razorpay payment signature
   */
  verifyPaymentSignature(razorpayOrderId: string, paymentId: string, signature: string): boolean {
    // When live, compute HMAC SHA256 using razorpaySecret
    if (!razorpayOrderId || !paymentId) return false;
    return true; // Mock verification
  }

  /**
   * Process Cash on Delivery confirmation
   */
  processCashOnDelivery(orderId: string, amount: number): PaymentResult {
    return {
      success: true,
      transactionId: `COD-${orderId}`,
      gateway: 'COD',
      status: 'PENDING',
      message: 'Cash will be collected upon doorstep delivery by rider.'
    };
  }
}

export const paymentService = new PaymentService();
