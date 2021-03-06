package org.hornetq.rest.test;

import org.hornetq.rest.queue.QueueDeployment;
import org.hornetq.rest.util.Constants;
import org.hornetq.rest.util.CustomHeaderLinkStrategy;
import org.hornetq.rest.util.LinkHeaderLinkStrategy;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.Link;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.jboss.resteasy.test.TestPortProvider.*;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class AckQueueTest extends MessageTestBase
{

   @BeforeClass
   public static void setup() throws Exception
   {
      QueueDeployment deployment1 = new QueueDeployment("testQueue", true);
      manager.getQueueManager().deploy(deployment1);
   }

   @Test
   public void testAckTimeoutX2() throws Exception
   {
      QueueDeployment deployment = new QueueDeployment();
      deployment.setConsumerSessionTimeoutSeconds(1);
      deployment.setDuplicatesAllowed(true);
      deployment.setDurableSend(false);
      deployment.setName("testAck");
      manager.getQueueManager().deploy(deployment);

      manager.getQueueManager().setLinkStrategy(new LinkHeaderLinkStrategy());
      testAckTimeout();
      manager.getQueueManager().setLinkStrategy(new CustomHeaderLinkStrategy());
      testAckTimeout();
   }

   public void testAckTimeout() throws Exception
   {
      ClientRequest request = new ClientRequest(generateURL("/queues/testAck"));

      ClientResponse response = request.head();
      Assert.assertEquals(200, response.getStatus());
      Link sender = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), response, "create");
      System.out.println("create: " + sender);
      Link consumers = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), response, "pull-consumers");
      System.out.println("pull: " + consumers);
      response = consumers.request().formParameter("autoAck", "false").post();
      Link consumeNext = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), response, "acknowledge-next");
      System.out.println("poller: " + consumeNext);

      {
         ClientResponse res = sender.request().body("text/plain", Integer.toString(1)).post();
         Assert.assertEquals(201, res.getStatus());


         res = consumeNext.request().post(String.class);
         Assert.assertEquals(200, res.getStatus());
         Link ack = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), res, "acknowledgement");
         System.out.println("ack: " + ack);
         Assert.assertNotNull(ack);
         Link session = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), res, "consumer");
         System.out.println("session: " + session);
         consumeNext = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), res, "acknowledge-next");
         System.out.println("consumeNext: " + consumeNext);

         // test timeout
         Thread.sleep(2000);

         ClientResponse ackRes = ack.request().formParameter("acknowledge", "true").post();
         Assert.assertEquals(412, ackRes.getStatus());
         System.out.println("**** Successfully failed ack");
         consumeNext = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), ackRes, "acknowledge-next");
         System.out.println("consumeNext: " + consumeNext);
      }
      {
         ClientResponse res = consumeNext.request().header(Constants.WAIT_HEADER, "2").post(String.class);
         Assert.assertEquals(200, res.getStatus());
         Link ack = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), res, "acknowledgement");
         System.out.println("ack: " + ack);
         Assert.assertNotNull(ack);
         Link session = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), res, "consumer");
         System.out.println("session: " + session);
         consumeNext = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), res, "acknowledge-next");
         System.out.println("consumeNext: " + consumeNext);

         ClientResponse ackRes = ack.request().formParameter("acknowledge", "true").post();
         if (ackRes.getStatus() != 204)
         {
            System.out.println(ackRes.getEntity(String.class));
         }
         Assert.assertEquals(204, ackRes.getStatus());


         Assert.assertEquals(204, session.request().delete().getStatus());
      }


   }

   @Test
   public void testSuccessFirstX2() throws Exception
   {
      manager.getQueueManager().setLinkStrategy(new LinkHeaderLinkStrategy());
      testSuccessFirst(1);
      manager.getQueueManager().setLinkStrategy(new CustomHeaderLinkStrategy());
      testSuccessFirst(3);
   }

   public void testSuccessFirst(int start) throws Exception
   {
      ClientRequest request = new ClientRequest(generateURL("/queues/testQueue"));

      ClientResponse response = request.head();
      Assert.assertEquals(200, response.getStatus());
      Link sender = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), response, "create");
      System.out.println("create: " + sender);
      Link consumers = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), response, "pull-consumers");
      System.out.println("pull: " + consumers);
      response = consumers.request().formParameter("autoAck", "false").post();
      Link consumeNext = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), response, "acknowledge-next");
      System.out.println("poller: " + consumeNext);

      String data = Integer.toString(start);
      System.out.println("Sending: " + data);
      ClientResponse res = sender.request().body("text/plain", data).post();
      Assert.assertEquals(201, res.getStatus());

      System.out.println("call ack next");
      res = consumeNext.request().post(String.class);
      Assert.assertEquals(200, res.getStatus());
      Assert.assertEquals(Integer.toString(start++), res.getEntity());
      Link ack = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), res, "acknowledgement");
      System.out.println("ack: " + ack);
      Assert.assertNotNull(ack);
      Link session = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), res, "consumer");
      System.out.println("session: " + session);
      consumeNext = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), res, "acknowledge-next");
      System.out.println("consumeNext: " + consumeNext);
      ClientResponse ackRes = ack.request().formParameter("acknowledge", "true").post();
      Assert.assertEquals(204, ackRes.getStatus());
      consumeNext = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), ackRes, "acknowledge-next");

      System.out.println("sending next...");
      String data2 = Integer.toString(start);
      System.out.println("Sending: " + data2);
      res = sender.request().body("text/plain", data2).post();
      Assert.assertEquals(201, res.getStatus());

      System.out.println(consumeNext);
      res = consumeNext.request().header(Constants.WAIT_HEADER, "10").post(String.class);
      Assert.assertEquals(200, res.getStatus());
      Assert.assertEquals(Integer.toString(start++), res.getEntity());
      ack = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), res, "acknowledgement");
      System.out.println("ack: " + ack);
      Assert.assertNotNull(ack);
      session = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), res, "consumer");
      System.out.println("session: " + session);
      ackRes = ack.request().formParameter("acknowledge", "true").post();
      Assert.assertEquals(204, ackRes.getStatus());
      consumeNext = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), ackRes, "acknowledge-next");
      System.out.println("consumeNext: " + consumeNext);
      res = consumeNext.request().post(String.class);

      System.out.println(res.getStatus());

      Assert.assertEquals(204, session.request().delete().getStatus());
   }

   @Test
   public void testPullX2() throws Exception
   {
      manager.getQueueManager().setLinkStrategy(new LinkHeaderLinkStrategy());
      testPull(1);
      manager.getQueueManager().setLinkStrategy(new CustomHeaderLinkStrategy());
      testPull(4);
   }

   public void testPull(int start) throws Exception
   {
      ClientRequest request = new ClientRequest(generateURL("/queues/testQueue"));

      ClientResponse response = request.head();
      Assert.assertEquals(200, response.getStatus());
      Link sender = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), response, "create");
      System.out.println("create: " + sender);
      Link consumers = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), response, "pull-consumers");
      System.out.println("pull: " + consumers);
      response = consumers.request().formParameter("autoAck", "false").post();
      Link consumeNext = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), response, "acknowledge-next");
      System.out.println("poller: " + consumeNext);

      ClientResponse<String> res = consumeNext.request().post(String.class);
      Assert.assertEquals(503, res.getStatus());
      consumeNext = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), res, "acknowledge-next");
      System.out.println(consumeNext);
      Assert.assertEquals(201, sender.request().body("text/plain", Integer.toString(start)).post().getStatus());
      res = consumeNext.request().post(String.class);
      Assert.assertEquals(200, res.getStatus());
      Assert.assertEquals(Integer.toString(start++), res.getEntity());
      Link ack = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), res, "acknowledgement");
      System.out.println("ack: " + ack);
      ClientResponse ackRes = ack.request().formParameter("acknowledge", "true").post();
      Assert.assertEquals(204, ackRes.getStatus());
      Assert.assertEquals(503, consumeNext.request().post().getStatus());
      Assert.assertEquals(201, sender.request().body("text/plain", Integer.toString(start)).post().getStatus());
      Assert.assertEquals(201, sender.request().body("text/plain", Integer.toString(start + 1)).post().getStatus());


      res = consumeNext.request().post(String.class);
      Assert.assertEquals(200, res.getStatus());
      Assert.assertEquals(Integer.toString(start++), res.getEntity());
      ack = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), res, "acknowledgement");
      System.out.println("ack: " + ack);
      ackRes = ack.request().formParameter("acknowledge", "true").post();
      Assert.assertEquals(204, ackRes.getStatus());

      res = consumeNext.request().post(String.class);
      Assert.assertEquals(200, res.getStatus());
      Assert.assertEquals(Integer.toString(start++), res.getEntity());
      ack = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), res, "acknowledgement");
      System.out.println("ack: " + ack);
      ackRes = ack.request().formParameter("acknowledge", "true").post();
      Assert.assertEquals(204, ackRes.getStatus());
      Link session = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), ackRes, "consumer");

      Assert.assertEquals(503, consumeNext.request().post().getStatus());
      System.out.println(session);
      Assert.assertEquals(204, session.request().delete().getStatus());
   }

   @Test
   public void testReconnectX2() throws Exception
   {
      manager.getQueueManager().setLinkStrategy(new LinkHeaderLinkStrategy());
      testReconnect();
      manager.getQueueManager().setLinkStrategy(new CustomHeaderLinkStrategy());
      testReconnect();
   }

   public void testReconnect() throws Exception
   {
      ClientRequest request = new ClientRequest(generateURL("/queues/testQueue"));

      ClientResponse response = request.head();
      Assert.assertEquals(200, response.getStatus());
      Link sender = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), response, "create");
      System.out.println("create: " + sender);
      Link consumers = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), response, "pull-consumers");
      System.out.println("pull: " + consumers);
      response = consumers.request().formParameter("autoAck", "false").post();
      Link consumeNext = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), response, "acknowledge-next");
      System.out.println("poller: " + consumeNext);

      ClientResponse res = sender.request().body("text/plain", Integer.toString(1)).post();
      Assert.assertEquals(201, res.getStatus());

      res = consumeNext.request().post(String.class);
      Assert.assertEquals(200, res.getStatus());
      Link ack = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), res, "acknowledgement");
      System.out.println("ack: " + ack);
      Assert.assertNotNull(ack);
      Link session = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), res, "consumer");
      System.out.println("session: " + session);
      consumeNext = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), res, "acknowledge-next");
      System.out.println("consumeNext: " + consumeNext);
      ClientResponse ackRes = ack.request().formParameter("acknowledge", "true").post();
      Assert.assertEquals(204, ackRes.getStatus());
      consumeNext = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), ackRes, "acknowledge-next");
      System.out.println("before close session consumeNext: " + consumeNext);

      // test reconnect with a disconnected acknowledge-next
      Assert.assertEquals(204, session.request().delete().getStatus());

      res = sender.request().body("text/plain", Integer.toString(2)).post();
      Assert.assertEquals(201, res.getStatus());


      res = consumeNext.request().header(Constants.WAIT_HEADER, "10").post(String.class);
      Assert.assertEquals(200, res.getStatus());
      ack = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), res, "acknowledgement");
      System.out.println("ack: " + ack);
      Assert.assertNotNull(ack);
      ackRes = ack.request().formParameter("acknowledge", "true").post();
      Assert.assertEquals(204, ackRes.getStatus());
      session = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), ackRes, "consumer");
      consumeNext = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), ackRes, "acknowledge-next");
      System.out.println("session: " + session);

      // test reconnect with disconnected acknowledge

      res = sender.request().body("text/plain", Integer.toString(3)).post();
      Assert.assertEquals(201, res.getStatus());
      res = consumeNext.request().header(Constants.WAIT_HEADER, "10").post(String.class);
      Assert.assertEquals(200, res.getStatus());
      ack = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), res, "acknowledgement");
      System.out.println("ack: " + ack);
      Assert.assertNotNull(ack);

      Assert.assertEquals(204, session.request().delete().getStatus());

      ackRes = ack.request().formParameter("acknowledge", "true").post();
      Assert.assertEquals(412, ackRes.getStatus());
      session = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), ackRes, "consumer");
      consumeNext = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), ackRes, "acknowledge-next");
      res = consumeNext.request().header(Constants.WAIT_HEADER, "10").post(String.class);
      Assert.assertEquals(200, res.getStatus());
      ack = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), res, "acknowledgement");
      System.out.println("ack: " + ack);
      Assert.assertNotNull(ack);
      ackRes = ack.request().formParameter("acknowledge", "true").post();
      Assert.assertEquals(204, ackRes.getStatus());
      session = MessageTestBase.getLinkByTitle(manager.getQueueManager().getLinkStrategy(), ackRes, "consumer");

      Assert.assertEquals(204, session.request().delete().getStatus());
   }

}