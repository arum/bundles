package uk.co.arum.osgi.amf3.sample.test001
{
	import mx.collections.ArrayCollection;
	
	[RemoteClass(alias="uk.co.arum.osgi.amf3.sample.test001.MidLevel")]
	public class MidLevel
	{
		
		public var name:String;
		
		public var bottom:BottomLevel;
		
		public function MidLevel()
		{
		}

	}
}